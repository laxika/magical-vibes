package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesOnlyLandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "69")
public class ImprisonedInTheMoon extends Card {

    public ImprisonedInTheMoon() {
        // Enchant creature, land, or planeswalker
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsLandPredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                "Target must be a creature, land, or planeswalker"
        ))
                // Enchanted permanent is a colorless land …
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentBecomesOnlyLandEffect())
                .addEffect(EffectSlot.STATIC, new BecomeColorlessEffect(GrantScope.ENCHANTED_PERMANENT))
                // … loses all other abilities …
                .addEffect(EffectSlot.STATIC, new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_PERMANENT))
                // … and has "{T}: Add {C}"
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(true, null, List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                                "{T}: Add {C}."),
                        GrantScope.ENCHANTED_PERMANENT
                ));
    }
}
