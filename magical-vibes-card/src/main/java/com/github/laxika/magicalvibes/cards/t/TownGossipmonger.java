package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.IncitedRabble;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "46")
public class TownGossipmonger extends Card {

    public TownGossipmonger() {
        IncitedRabble backFace = new IncitedRabble();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // {T}, Tap an untapped creature you control: Transform this creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapCreatureCost(new PermanentTruePredicate(), true, false),
                        new TransformSelfEffect()),
                "{T}, Tap an untapped creature you control: Transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "IncitedRabble";
    }
}
