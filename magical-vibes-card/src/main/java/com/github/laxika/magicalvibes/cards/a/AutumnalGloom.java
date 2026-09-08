package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "194")
public class AutumnalGloom extends Card {

    public AutumnalGloom() {
        setBackFaceCard(new AncientOfTheEquinox());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new MillEffect(1, MillRecipient.CONTROLLER)),
                "{B}: Mill a card."
        ));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new Delirium(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "AncientOfTheEquinox";
    }
}
