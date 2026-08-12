package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "186")
public class MarduCharm extends Card {

    public MarduCharm() {
        var creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature.");
        var opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Mardu Charm deals 4 damage to target creature",
                        new DealDamageToTargetCreatureEffect(4), creatureFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Create two 1/1 white Warrior creature tokens. They gain first strike until end of turn",
                        new CreateTokenEffect(CardType.CREATURE, 2, "Warrior", 1, 1, CardColor.WHITE,
                                null, List.of(CardSubtype.WARRIOR), Set.of(), Set.of(), false, false,
                                Map.of(), List.of(), false, false, false, 0, Set.of(Keyword.FIRST_STRIKE))),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent reveals their hand. You choose a noncreature, nonland card from it. That player discards that card",
                        new ChooseCardsFromTargetHandEffect(
                                1, List.of(CardType.CREATURE, CardType.LAND), HandChoiceDestination.DISCARD),
                        opponentFilter)
        )));
    }
}
