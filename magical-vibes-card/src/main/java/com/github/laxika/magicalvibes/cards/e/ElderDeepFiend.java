package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "4")
public class ElderDeepFiend extends Card {

    public ElderDeepFiend() {
        // Flash is loaded from Scryfall

        // Emerge {5}{U}{U} — sacrifice a creature and pay the emerge cost reduced by that
        // creature's mana value (generic only; colored components cannot be reduced).
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{5}{U}{U}"),
                new SacrificePermanentsCost(1, new PermanentIsCreaturePredicate())
        ), true));

        // When you cast this spell, tap up to four target permanents.
        target(new PermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target must be a permanent"
        ), 0, 4)
                .addEffect(EffectSlot.ON_SELF_CAST, new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
