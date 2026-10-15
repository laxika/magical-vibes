package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "116")
public class HeirOfFalkenrath extends Card {

    public HeirOfFalkenrath() {
        setBackFaceCard(new HeirToTheNight());

        // Discard a card: Transform this creature. Activate only once each turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardCardTypeCost(null, null), new TransformSelfEffect()),
                "Discard a card: Transform this creature. Activate only once each turn.",
                1
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "HeirToTheNight";
    }
}
