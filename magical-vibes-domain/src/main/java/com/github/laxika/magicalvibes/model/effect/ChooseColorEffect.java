package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.List;

public interface ChooseColorEffect extends CardEffect {

    /**
     * The colors the choosing player may pick from. Defaults to all five; cards that restrict the
     * choice ("choose black or red" — Mangara's Equity) override it with a narrower list.
     */
    default List<CardColor> allowedColors() {
        return List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
    }
}
