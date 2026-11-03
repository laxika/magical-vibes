package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Delirium()),
                new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "AncientOfTheEquinox";
    }
}
