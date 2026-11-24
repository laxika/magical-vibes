package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;

import java.util.List;
import java.util.Objects;

/**
 * "Specialize {N}" (digital only): the source permanent perpetually becomes its specialized face for {@code color},
 * the color of the card discarded to activate the ability. The face replaces the card the permanent shows and goes
 * with it to other zones; its "when this creature specializes" ability triggers as it specializes.
 *
 * <p>A permanent that has specialized, or that became another card since the ability was activated, doesn't
 * specialize. Each card writes its own faces ({@link Faces}), so every face keeps its own Oracle text and
 * abilities.</p>
 *
 * @param color the color the permanent specializes into
 * @param faces the card's specialized faces
 */
public record SpecializeEffect(CardColor color, Faces faces) implements CardEffect {

    public SpecializeEffect {
        Objects.requireNonNull(color, "color");
        Objects.requireNonNull(faces, "faces");
    }

    /** One card's specialized faces. */
    @FunctionalInterface
    public interface Faces {

        /**
         * Writes the face for {@code color} onto {@code face}, a runtime copy of the card with its abilities cleared
         * (its characteristics, rules text, abilities and target groups), and returns the effects of the face's
         * "when this creature specializes" ability, empty when the face has none.
         */
        List<CardEffect> writeFace(Card face, CardColor color);
    }
}
