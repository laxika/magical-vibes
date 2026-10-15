package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentControllerCantCastSpellTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "177")
public class BrandOfIllOmen extends Card {

    public BrandOfIllOmen() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // Cumulative upkeep {R}
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{R}"))

                // Enchanted creature's controller can't cast creature spells.
                .addEffect(EffectSlot.STATIC,
                        new EnchantedPermanentControllerCantCastSpellTypeEffect(Set.of(CardType.CREATURE)));
    }
}
