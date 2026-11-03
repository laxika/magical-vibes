package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "174")
public class ArmoredKincaller extends Card {

    public ArmoredKincaller() {
        PermanentHasSubtypePredicate dinosaur = new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR);
        ControlsAnotherPermanent controlsAnotherDinosaur = new ControlsAnotherPermanent(dinosaur);

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayRevealSubtypeFromHandEffect(
                        CardSubtype.DINOSAUR,
                        ConditionalEffect.unless(new NotCondition(controlsAnotherDinosaur), new GainLifeEffect(3)),
                        "Reveal a Dinosaur card from your hand?"),
                ConditionalEffect.unless(controlsAnotherDinosaur, new GainLifeEffect(3)));
    }
}
