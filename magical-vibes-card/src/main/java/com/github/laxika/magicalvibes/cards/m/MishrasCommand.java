package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsUpToThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "141")
public class MishrasCommand extends Card {

    public MishrasCommand() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Choose target player. They may discard up to X cards, then draw a card for each card discarded this way",
                        new TargetPlayerDiscardsUpToThenDrawsThatManyEffect(new XValue()),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Mishra's Command deals X damage to target creature",
                        new DealDamageToTargetCreatureEffect(new XValue()),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Mishra's Command deals X damage to target planeswalker",
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(new XValue()),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsPlaneswalkerPredicate(),
                                "Target must be a planeswalker.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +X/+0 and gains haste until end of turn",
                        List.of(
                                new BoostTargetCreatureEffect(new XValue(), new Fixed(0)),
                                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                        TargetFilters.creature())
        ), 2));
    }
}
