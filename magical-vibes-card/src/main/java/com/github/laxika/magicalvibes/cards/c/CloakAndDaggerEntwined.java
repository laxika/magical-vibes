package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTargetHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "211")
public class CloakAndDaggerEntwined extends Card {

    public CloakAndDaggerEntwined() {
        PlayerPredicateTargetFilter opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
        PermanentIsCreaturePredicate creaturePredicate = new PermanentIsCreaturePredicate();
        PermanentPredicateTargetFilter creatureFilter = new PermanentPredicateTargetFilter(
                creaturePredicate, "Target must be a creature that player controls");

        SpellTarget opponentTarget = target(opponentFilter);
        SpellTarget creatureTarget = target(creatureFilter, 0, 1);

        RevealTargetHandEffect revealHand = new RevealTargetHandEffect();
        ChooseCardsFromTargetHandEffect exileFromHand = new ChooseCardsFromTargetHandEffect(
                new Fixed(1), List.of(CardType.LAND), List.of(), HandChoiceDestination.EXILE,
                true, null, 0, true, false, false, false, false, false);
        ExileTargetPermanentUntilSourceLeavesEffect exileCreature =
                new ExileTargetPermanentUntilSourceLeavesEffect(false, creaturePredicate);
        ChooseOneAtResolutionEffect choice = new ChooseOneAtResolutionEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile a nonland card from their hand.", exileFromHand),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the chosen creature.", exileCreature)
        )));

        opponentTarget.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, revealHand);
        creatureTarget.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, choice);
        registerEffectTargetIndex(exileFromHand, opponentTarget.getIndex());
        registerEffectTargetIndex(exileCreature, creatureTarget.getIndex());

        setMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET);
    }
}
