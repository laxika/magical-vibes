package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "245")
@CardRegistration(set = "WOE", collectorNumber = "212")
public class RubyDaringTracker extends Card {

    public RubyDaringTracker() {
        // "Whenever Ruby attacks while you control a creature with power 4 or greater" — the "while"
        // condition is checked only when the attack triggers, and an uncrewed Vehicle is not a creature.
        addEffect(EffectSlot.ON_ATTACK, ConditionalEffect.atTriggerTime(
                new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(4)))),
                new BoostSelfEffect(2, 2)));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
