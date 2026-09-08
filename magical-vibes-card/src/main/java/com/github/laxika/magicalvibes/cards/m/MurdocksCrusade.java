package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TeamworkCostPaid;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "24")
public class MurdocksCrusade extends Card {

    public MurdocksCrusade() {
        PermanentAllOfPredicate creatureWithToughnessAtLeastFour = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentToughnessAtLeastPredicate(4)
        ));
        PermanentAllOfPredicate enchantmentWithManaValueAtLeastFour = new PermanentAllOfPredicate(List.of(
                new PermanentIsEnchantmentPredicate(),
                new PermanentMinManaValuePredicate(4)
        ));

        addEffect(EffectSlot.SPELL, new TeamworkCost(4));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Street Justice — Exile target creature with toughness 4 or greater",
                        new ExileTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                creatureWithToughnessAtLeastFour,
                                "Target must be a creature with toughness 4 or greater"
                        )),
                new ChooseOneEffect.ChooseOneOption(
                        "Legal Justice — Exile target enchantment with mana value 4 or greater",
                        new ExileTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                enchantmentWithManaValueAtLeastFour,
                                "Target must be an enchantment with mana value 4 or greater"
                        ))
        ), false, 1, 2, true, false, new TeamworkCostPaid()));
    }
}
