package org.example;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

@Log4j2
@Plugin(name = "CustomAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public final class CustomAppender extends AbstractAppender {

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
            log.fatal("Error");
            log.fatal(Arrays.toString(e.getStackTrace()));
            System.exit(1);
        }
    }

    @PluginFactory
    public static CustomAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) boolean ignoreExceptions
    ) {
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
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
