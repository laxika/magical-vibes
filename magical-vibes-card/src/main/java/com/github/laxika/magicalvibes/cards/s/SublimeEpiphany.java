package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "74")
public class SublimeEpiphany extends Card {

    public SublimeEpiphany() {
        var abilityFilter = new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.ACTIVATED_ABILITY,
                        StackEntryType.TRIGGERED_ABILITY)),
                "Target must be an activated or triggered ability.");
        var playerFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell",
                        new CounterSpellEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target activated or triggered ability",
                        new CounterSpellEffect(),
                        abilityFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target nonland permanent to its owner's hand",
                        ReturnToHandEffect.target(),
                        TargetFilters.nonlandPermanent()),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a token that's a copy of target creature you control",
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        TargetFilters.creatureYouControl()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player draws a card",
                        new DrawCardForTargetPlayerEffect(1, false, true),
                        playerFilter)
        )));
    }
}
