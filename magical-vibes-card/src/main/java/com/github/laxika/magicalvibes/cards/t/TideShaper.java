package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.TrackedLandsBecomeBasicLandTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH2", collectorNumber = "72")
public class TideShaper extends Card {

    public TideShaper() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}"));
        addEffect(EffectSlot.STATIC, new TrackedLandsBecomeBasicLandTypeEffect(CardSubtype.ISLAND));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                new StaticBoostEffect(1, 1, GrantScope.SELF)));

        targetWhenKicked(TargetFilters.land(), 0, 0, 1, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new Kicked(),
                        new TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect(CardSubtype.ISLAND)));
    }
}
