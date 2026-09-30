package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "5")
public class TalionsThroneguard extends Card {

    public TalionsThroneguard() {
        addEffect(EffectSlot.STATIC, new KickerEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentIsLandPredicate()
                )),
                "an artifact, enchantment, or land"
        ));
        targetWhenKicked(TargetFilters.nonlandPermanent(), 0, 0, 1, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new Kicked(), new ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect()));
    }
}
