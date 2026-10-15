package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarborGuardian.class, Forest.class, BayFalcon.class, JaceBeleren.class})
class HarborGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, the defending player may draw a card (accept)")
    void defendingPlayerDraws() {
        addCreatureReady(player1, new HarborGuardian());
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Defending player may decline the draw")
    void defendingPlayerDeclines() {
        addCreatureReady(player1, new HarborGuardian());
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore);
    }

    @Test
    @DisplayName("When the other player attacks, the defending player receives the choice")
    void otherPlayerAttacks() {
        addCreatureReady(player2, new HarborGuardian());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    void reachAllowsBlockingFlyingCreatureWithoutOfferingDraw() {
        addCreatureReady(player1, new BayFalcon());
        Permanent guardian = addCreatureReady(player2, new HarborGuardian());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(guardian.isBlocking()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void attackTriggerSurvivesGuardianLeavingBattlefield() {
        Permanent guardian = addCreatureReady(player1, new HarborGuardian());
        harness.setLibrary(player2, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(guardian);
        gd.playerGraveyards.get(player1.getId()).add(guardian.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    void eachAttackingGuardianOffersAnIndependentChoice() {
        addCreatureReady(player1, new HarborGuardian());
        addCreatureReady(player1, new HarborGuardian());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player2.getId()).size();
        int attackerHandBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(attackerHandBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @CardUsed({JaceBeleren.class})
    void attackingPlaneswalkerOffersDrawToItsController() {
        addCreatureReady(player1, new HarborGuardian());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player2, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackAtPlaneswalker(planeswalker);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @CardUsed({JaceBeleren.class})
    void defendingPlayerStillMayDrawAfterAttackedPlaneswalkerLeaves() {
        addCreatureReady(player1, new HarborGuardian());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player2, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackAtPlaneswalker(planeswalker);
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    private void declareAttackAtPlaneswalker(Permanent planeswalker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
    }

}
