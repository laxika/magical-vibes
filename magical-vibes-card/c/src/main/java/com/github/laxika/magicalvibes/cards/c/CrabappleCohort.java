package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "109")
public class CrabappleCohort extends Card {

    public CrabappleCohort() {
        // This creature gets +1/+1 as long as you control another green creature.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControlsAnotherPermanent(new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), new PermanentColorInPredicate(Set.of(CardColor.GREEN))))), new StaticBoostEffect(1, 1, GrantScope.SELF)));
    }
}
