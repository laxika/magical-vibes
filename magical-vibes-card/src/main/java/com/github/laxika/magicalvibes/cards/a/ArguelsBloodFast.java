package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TempleOfAclazotz;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeAtOrBelowThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "90")
public class ArguelsBloodFast extends Card {

    public ArguelsBloodFast() {
        // Set up back face
        TempleOfAclazotz backFace = new TempleOfAclazotz();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // {1}{B}, Pay 2 life: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{B}",
                List.of(new PayLifeCost(2), new DrawCardEffect()),
                "{1}{B}, Pay 2 life: Draw a card."
        ));

        // At the beginning of your upkeep, if you have 5 or less life,
        // you may transform Arguel's Blood Fast.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ControllerLifeAtOrBelowThresholdConditionalEffect(5,
                        new MayEffect(new TransformSelfEffect(), "Transform Arguel's Blood Fast?")));
    }

    @Override
    public String getBackFaceClassName() {
        return "TempleOfAclazotz";
    }
}
