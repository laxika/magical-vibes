package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "208")
public class TemurCharm extends Card {

    public TemurCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control gets +1/+1 until end of turn. It fights target creature you don't control",
                        List.of(new BoostTargetCreatureEffect(1, 1), new FightTargetsEffect()),
                        List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls())),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell unless its controller pays {3}",
                        new CounterUnlessPaysEffect(3),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.CREATURE_SPELL,
                                        StackEntryType.INSTANT_SPELL,
                                        StackEntryType.SORCERY_SPELL,
                                        StackEntryType.ARTIFACT_SPELL,
                                        StackEntryType.ENCHANTMENT_SPELL,
                                        StackEntryType.PLANESWALKER_SPELL,
                                        StackEntryType.BATTLE_SPELL)),
                                "Target must be a spell.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures with power 3 or less can't block this turn",
                        new CantBlockThisTurnEffect(
                                TapUntapScope.ALL_CREATURES,
                                new PermanentPowerAtMostPredicate(3)))
        )));
    }
}
