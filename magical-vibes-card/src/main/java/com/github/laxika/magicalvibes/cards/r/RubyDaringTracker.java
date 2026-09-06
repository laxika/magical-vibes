package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "FDN", collectorNumber = "245")
@CardRegistration(set = "WOE", collectorNumber = "212")
public class RubyDaringTracker extends Card {

    public RubyDaringTracker() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new ControlsPermanent(new PermanentPowerAtLeastPredicate(4)),
                new BoostSelfEffect(2, 2)));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
