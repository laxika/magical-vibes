package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "135")
public class OniPossession extends Card {

    public OniPossession() {
        target(TargetFilters.creature())
                // At the beginning of your upkeep, sacrifice a creature.
                // Bare creature predicate = the single-select "sacrifice a creature" primitive;
                // any creature qualifies, including the enchanted one.
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificePermanentsEffect(
                        1, new PermanentIsCreaturePredicate(), SacrificeRecipient.CONTROLLER))
                // Enchanted creature gets +3/+3 and has trample.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        3, 3, Set.of(Keyword.TRAMPLE), GrantScope.ENCHANTED_CREATURE))
                // Enchanted creature is a Demon Spirit (type-replacing, not additive).
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentBecomesTypeEffect(
                        List.of(CardSubtype.DEMON, CardSubtype.SPIRIT)));
    }
}
