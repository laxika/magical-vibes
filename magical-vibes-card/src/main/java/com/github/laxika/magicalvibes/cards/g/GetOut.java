package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "60")
public class GetOut extends Card {

    public GetOut() {
        PermanentPredicate creatureOrEnchantmentYouOwn = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate())),
                new PermanentOwnedBySourceControllerPredicate()));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target creature or enchantment spell",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryCardTypeInPredicate(Set.of(CardType.CREATURE, CardType.ENCHANTMENT)),
                                "Target must be a creature or enchantment spell.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Return one or two target creatures and/or enchantments you own to your hand",
                        List.of(ReturnToHandEffect.target(creatureOrEnchantmentYouOwn)),
                        new PermanentPredicateTargetFilter(
                                creatureOrEnchantmentYouOwn,
                                "Targets must be creatures and/or enchantments you own."),
                        null,
                        1,
                        2,
                        false,
                        null)
        )));
    }
}
