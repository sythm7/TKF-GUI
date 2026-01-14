package fr.sythm.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;

/** Represents a line of text (with or without colors/multiple colors)
 *  This is used for adding text to a {@link SimpleScoreboard},
 *  and can also be used for anything else as this class can replace the need
 *  of instantiating a {@link TextComponent} (see {@link Line#asComponent()})
 */
public class Line {

    /** The component of the line */
    private TextComponent component;

    /** Creates an empty line */
    public Line() {
        this.component = Component.text("");
    }

    /** Creates a line with the specified words
     * @param words The words of the line
     */
    public Line(String words) {
        this(words, null);
    }

    /** Creates a line with the specified words and color
     * @param words The words of the line
     * @param color The color of the line
     */
    public Line(String words, Color color) {
        this();
        this.add(words, color);
    }

    /** Adds words to the line
     * @param words The words to add
     * @return The current {@link Line} instance
     */
    public Line add(String words) {
        return this.add(words, null);
    }

    /** Adds words with a specific color to the line
     * @param words The words to add
     * @param color The color of the words to add
     * @return The current {@link Line} instance
     */
    public Line add(String words, Color color) {
        this.component = this.component.append(Component.text(words).color(
                color != null ? TextColor.color(color.asRGB()) : null)
        );
        return this;
    }

    /** Gets the {@link TextComponent} representation of the line
     * @return The {@link TextComponent} representation of the line
     */
    public TextComponent asComponent() {
        return this.component;
    }
}
