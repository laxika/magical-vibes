package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.List;

public interface ChooseColorEffect extends CardEffect {

    /** Number of distinct colors this as-enters choice must collect. */
    default int choicesRequired() {
        return 1;
    }

    /** Maximum number of distinct colors this choice may collect. */
    default int choicesMax() {
        return choicesRequired();
    }

    /** Whether the color options are limited to colors represented by cards in the controller's hand. */
    default boolean colorsFromControllerHand() {
        return false;
    }

    /** Whether this permanent has already completed this choice. */
    default boolean choiceComplete(Permanent permanent) {
        return permanent != null && (permanent.isChosenColorChoiceMade()
                || (choicesRequired() > 0 && choicesRequired() == 1 && permanent.getChosenColor() != null)
                || (choicesRequired() > 0 && permanent.getChosenColors().size() >= choicesRequired()));
    }

    /**
     * The colors the choosing player may pick from. Defaults to all five; cards that restrict the
     * choice ("choose black or red" — Mangara's Equity) override it with a narrower list.
     */
    default List<CardColor> allowedColors() {
        return List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
    }
}
