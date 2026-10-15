package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSuspected;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;

@CardRegistration(set = "MKM", collectorNumber = "143")
public class RubblebeltBraggart extends Card {

    public RubblebeltBraggart() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new NotCondition(new SourceIsSuspected()),
                new MayEffect(new SuspectEffect(GrantScope.SELF), "Suspect Rubblebelt Braggart?")));
    }
}
