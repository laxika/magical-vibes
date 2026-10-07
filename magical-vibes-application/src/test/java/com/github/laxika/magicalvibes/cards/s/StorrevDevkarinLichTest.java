package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.c.CentaurNurturer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.cards.n.NagaEternal;
import com.github.laxika.magicalvibes.cards.t.TotallyLost;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StorrevDevkarinLich.class, GrizzlyBears.class, ChandraNalaar.class,
        NarsetParterOfVeils.class, HillGiant.class, CentaurNurturer.class,
        NagaEternal.class, TotallyLost.class})
class StorrevDevkarinLichTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureCardToHandAfterDealingCombatDamageToPlayer() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        Permanent storrev = addCreatureReady(player1, new StorrevDevkarinLich());
        storrev.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void returnsTargetPlaneswalkerCardAfterDealingCombatDamageToPlaneswalker() {
        Card target = new ChandraNalaar();
        harness.setGraveyard(player1, List.of(target));
        Permanent storrev = addCreatureReady(player1, new StorrevDevkarinLich());
        Permanent narset = addCreatureReady(player2, new NarsetParterOfVeils());
        narset.setCounterCount(CounterType.LOYALTY, 6);
        storrev.setAttacking(true);

        declareAttackersAtPlaneswalker(narset);
        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void cannotTargetACardPutIntoAGraveyardDuringTheSameCombat() {
        Permanent storrev = addCreatureReady(player1, new StorrevDevkarinLich());
        Permanent dyingAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        storrev.setAttacking(true);
        dyingAttacker.setAttacking(true);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dyingAttacker.getCard().getId()));
    }

    @Test
    void trampleDamageStillTriggersWhenStorrevDiesInCombat() {
        Card target = new NagaEternal();
        harness.setGraveyard(player1, List.of(target));
        Permanent storrev = addCreatureReady(player1, new StorrevDevkarinLich());
        Permanent firstBlocker = addCreatureReady(player2, new NagaEternal());
        Permanent secondBlocker = addCreatureReady(player2, new NagaEternal());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstBlocker.getId(), 2, secondBlocker.getId(), 2, player2.getId(), 1));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(storrev.getCard().getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(storrev.getCard().getId()));
    }

    @Test
    void dealingCombatDamageOnlyToCreaturesDoesNotReturnACard() {
        Card target = new NagaEternal();
        harness.setGraveyard(player1, List.of(target));
        addCreatureReady(player1, new StorrevDevkarinLich());
        Permanent firstBlocker = addCreatureReady(player2, new CentaurNurturer());
        Permanent secondBlocker = addCreatureReady(player2, new CentaurNurturer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(firstBlocker.getId(), 4, secondBlocker.getId(), 1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void cannotReturnAnInstantOrACardFromAnOpponentsGraveyard() {
        Card instant = new TotallyLost();
        Card opponentsCreature = new NagaEternal();
        harness.setGraveyard(player1, List.of(instant));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        Permanent storrev = addCreatureReady(player1, new StorrevDevkarinLich());
        storrev.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(instant.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(opponentsCreature.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(instant.getId())
                || card.getId().equals(opponentsCreature.getId()));
    }

    @Test
    void doesNotReturnATargetThatLeavesTheGraveyardBeforeResolution() {
        Card target = new NagaEternal();
        harness.setGraveyard(player1, List.of(target));
        Permanent storrev = addCreatureReady(player1, new StorrevDevkarinLich());
        storrev.setAttacking(true);

        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(target.getId()));
    }

    private void declareAttackersAtPlaneswalker(Permanent planeswalker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
    }
}
