package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.List;

/**
 * "As this permanent enters, choose a color." The pick is stored on the permanent as its
 * {@code chosenColor}. Pass a narrower {@code allowedColors} list for cards that restrict the
 * choice ("choose black or red" — Mangara's Equity), or a {@code choicesRequired} above one for
 * cards that collect several distinct colors ("choose two colors" — Tablet of the Guilds).
 */
public record ChooseColorOnEnterEffect(int choicesRequired, List<CardColor> allowedColors)
        implements ChooseColorEffect {

    private static final List<CardColor> ALL_COLORS =
            List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);

    public ChooseColorOnEnterEffect() {
        this(1, ALL_COLORS);
    }

    /** "Choose {@code choicesRequired} different colors", unrestricted. */
    public ChooseColorOnEnterEffect(int choicesRequired) {
        this(choicesRequired, ALL_COLORS);
    }

    public ChooseColorOnEnterEffect(CardColor... allowedColors) {
        this(1, List.of(allowedColors));
    }
}
