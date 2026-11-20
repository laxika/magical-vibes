package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "39")
public class MercurialKite extends Card {

    public MercurialKite() {
        addTrigger(Triggers.dealsCombatDamageToCreature(), SequenceEffect.of(
                new TapPermanentsEffect(Subjects.target()),
                new SkipNextUntapEffect(Subjects.target())));
    }
}
