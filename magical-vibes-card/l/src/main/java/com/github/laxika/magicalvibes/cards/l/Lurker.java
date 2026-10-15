package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedOrBlockedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;

@CardRegistration(set = "DRK", collectorNumber = "80")
public class Lurker extends Card {

    public Lurker() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new SourceAttackedOrBlockedThisTurn()),
                TargetingRestrictionEffect.spells()));
    }
}
