package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.List;

/**
 * "As this permanent enters, choose a color." The pick is stored on the permanent as its
 * {@code chosenColor}. Pass a narrower {@code allowedColors} list for cards that restrict the
 * choice ("choose black or red" — Mangara's Equity).
 */
public record ChooseColorOnEnterEffect(List<CardColor> allowedColors) implements ChooseColorEffect {

    public ChooseColorOnEnterEffect() {
        this(List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN));
    }

    public ChooseColorOnEnterEffect(CardColor... allowedColors) {
        this(List.of(allowedColors));
    }
}
