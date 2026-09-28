package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromGraveyardAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "50")
public class CravingOfYeenoghu extends Card {

    public CravingOfYeenoghu() {
        // Enchant creature you control
        target(TargetFilters.creatureYouControl())
                // Enchanted creature gets +3/+2, has haste, and attacks each combat if able.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 2, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new MustAttackEffect());

        // {R}: Return this card from your graveyard to the battlefield attached to target creature you
        // control. Craving of Yeenoghu perpetually gains "Enchanted creature gets -1/-1." Activate only
        // as a sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(
                        new ReturnSourceFromGraveyardAttachedToTargetEffect(),
                        new PerpetuallyGrantStaticEffectToSourceEffect(
                                new StaticBoostEffect(-1, -1, GrantScope.ENCHANTED_CREATURE))),
                "{R}: Return this card from your graveyard to the battlefield attached to target creature "
                        + "you control. Craving of Yeenoghu perpetually gains \"Enchanted creature gets "
                        + "-1/-1.\" Activate only as a sorcery.",
                TargetFilters.creatureYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
