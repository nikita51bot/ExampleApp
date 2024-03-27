package org.example;

import java.io.IOException;
import java.io.Serializable;
import lombok.Getter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.status.StatusLogger;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

@Plugin(name = "CustomAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public final class CustomAppender extends AbstractAppender {

    private static final Logger LOGGER = StatusLogger.getLogger();

    @Getter
    private static CustomAppender instance;

    private Terminal terminal;
    @Getter
    private LineReader reader;


    private CustomAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
        try {
            this.terminal = TerminalBuilder.builder()
                    .dumb(true)
                    .build();
            this.reader = LineReaderBuilder.builder()
                    .appName(name)
                    .terminal(this.terminal)
                    .completer(new StringsCompleter("foo", "bar"))
                    .build();
            this.reader.setOpt(LineReader.Option.BRACKETED_PASTE);
            this.reader.unsetOpt(LineReader.Option.INSERT_TAB);
        } catch (IOException e) {
            LOGGER.fatal("Unable to create a CustomAppender instance.", e);
        }
    }

    @PluginFactory
    public static CustomAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) boolean ignoreExceptions,
            @PluginConfiguration Configuration config
    ) {
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout(config);
        }
        if (instance == null) {
            instance = new CustomAppender(name, filter, layout, ignoreExceptions);
        }
        return instance;
    }

    @Override
    public void append(LogEvent event) {
        if (this.terminal == null) {
            return;
        }
        String msg = this.getLayout().toSerializable(event).toString();
        if (this.reader == null) {
            this.terminal.writer().write(msg);
        } else {
            this.reader.printAbove(msg);
        }
    }
}
