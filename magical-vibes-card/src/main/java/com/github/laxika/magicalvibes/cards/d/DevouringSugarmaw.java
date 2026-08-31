package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HaveForDinner;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "224")
public class DevouringSugarmaw extends Card {

    public DevouringSugarmaw() {
        setBackFaceCard(new HaveForDinner());
        addCastingOption(new AdventureCast("{1}{W}"));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new SacrificePermanentCost(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsEnchantmentPredicate(),
                                new PermanentIsTokenPredicate())),
                        "an artifact, enchantment, or token", false),
                List.of(new TapPermanentsEffect(TapUntapScope.SELF)), true));
    }

    @Override
    public String getBackFaceClassName() {
        return "HaveForDinner";
    }
}
