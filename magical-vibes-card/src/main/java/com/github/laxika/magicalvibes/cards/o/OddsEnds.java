package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DIS", collectorNumber = "153")
public class OddsEnds extends Card {

    public OddsEnds() {
        TargetFilter instantOrSorcery = new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.INSTANT_SPELL,
                        StackEntryType.SORCERY_SPELL)),
                "Target must be an instant or sorcery spell.");
        TargetFilter anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Odds - Flip a coin. If it comes up heads, counter target instant or sorcery spell. "
                                + "If it comes up tails, copy that spell and you may choose new targets for the copy.",
                        new FlipCoinWinEffect(new CounterSpellEffect(), new CopySpellEffect()),
                        instantOrSorcery
                ).withManaCost("{U}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Ends - Target player sacrifices two attacking creatures of their choice.",
                        new SacrificeAttackingCreaturesEffect(2, 2),
                        anyPlayer
                ).withManaCost("{3}{R}{W}")
        )));
    }
}
