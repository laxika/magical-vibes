package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinForEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "134")
public class MutalithVortexBeast extends Card {

    public MutalithVortexBeast() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new FlipCoinForEachOpponentEffect(
                new DrawCardEffect(),
                DealDamageEffect.to(Subjects.triggeringPlayer(), 3)));
    }
}
