package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LCI", collectorNumber = "174")
public class ArmoredKincaller extends Card {

    public ArmoredKincaller() {
        PermanentHasSubtypePredicate dinosaur = new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR);
        ControlsAnotherPermanent controlsAnotherDinosaur = new ControlsAnotherPermanent(dinosaur);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayRevealSubtypeFromHandEffect(
                CardSubtype.DINOSAUR,
                new ConditionalEffect(new NotCondition(controlsAnotherDinosaur), new GainLifeEffect(3)),
                "Reveal a Dinosaur card from your hand?"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(controlsAnotherDinosaur, new GainLifeEffect(3)));
    }
}
