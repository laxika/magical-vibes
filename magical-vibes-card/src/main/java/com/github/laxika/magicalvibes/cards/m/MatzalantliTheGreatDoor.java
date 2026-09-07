package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TheCore;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.PermanentTypesInGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "256")
@CardRegistration(set = "LCI", collectorNumber = "387")
public class MatzalantliTheGreatDoor extends Card {

    public MatzalantliTheGreatDoor() {
        setBackFaceCard(new TheCore());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{T}: Draw a card, then discard a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new TransformSelfEffect()),
                "{4}, {T}: Transform Matzalantli. Activate only if there are four or more permanent types among cards in your graveyard."
        ).withActivationCondition(
                new PermanentTypesInGraveyardAtLeast(4),
                "Activate only if there are four or more permanent types among cards in your graveyard."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheCore";
    }
}
