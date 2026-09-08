package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "79")
public class Narcolepsy extends Card {

    public Narcolepsy() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new ConditionalEffect(
                        new EnchantedPermanentMatches(
                                new PermanentNotPredicate(new PermanentIsTappedPredicate()),
                                "enchanted creature is untapped"),
                        new TapPermanentsEffect(TapUntapScope.ENCHANTED)));
    }
}
