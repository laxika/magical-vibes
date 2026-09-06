package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "221")
public class DromokasCommand extends Card {

    public DromokasCommand() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Prevent all damage target instant or sorcery spell would deal this turn",
                        new PreventDamageFromTargetSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                                "Target must be an instant or sorcery spell.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player sacrifices an enchantment of their choice",
                        new SacrificePermanentsEffect(1, new PermanentIsEnchantmentPredicate(),
                                SacrificeRecipient.TARGET_PLAYER),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on target creature",
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control fights target creature you don't control",
                        List.of(new FightTargetsEffect(1, 2)),
                        List.of(TargetFilters.creatureYouControl(),
                                TargetFilters.creatureAnOpponentControls()))
        ), 2));
    }
}
