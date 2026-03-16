package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HomicidalBrute;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardTransformIfCreatureDiscardedEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "47")
public class CivilizedScholar extends Card {

    public CivilizedScholar() {
        // Set up back face
        HomicidalBrute backFace = new HomicidalBrute();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // {T}: Draw a card, then discard a card. If a creature card is discarded this way,
        // untap Civilized Scholar, then transform it.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DrawDiscardTransformIfCreatureDiscardedEffect()),
                "{T}: Draw a card, then discard a card. If a creature card is discarded this way, untap Civilized Scholar, then transform it."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "HomicidalBrute";
    }
}
