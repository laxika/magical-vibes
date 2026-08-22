package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "57")
public class MetamorphicBlast extends Card {

    public MetamorphicBlast() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{1}", "{3}")));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Until end of turn, target creature becomes a white Rabbit with base power and toughness 0/1",
                        List.of(
                                new GrantColorUntilEndOfTurnEffect(CardColor.WHITE),
                                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.RABBIT),
                                new SetBasePowerToughnessEffect(0, 1)),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player draws two cards",
                        new DrawCardForTargetPlayerEffect(2, false, true),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player."))
        )));
    }
}
