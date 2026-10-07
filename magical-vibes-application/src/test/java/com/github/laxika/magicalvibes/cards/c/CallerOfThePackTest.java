package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.t.TeferiTemporalArchmage;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallerOfThePack.class, TeferiTemporalArchmage.class, DoublingSeason.class})
class CallerOfThePackTest extends BaseCardTest {

    private Player player3;

    @Test
    @DisplayName("Myriad creates a tapped and attacking copy for another opponent")
    void myriadCreatesCopyForAnotherOpponentAndExilesItAtEndOfCombat() {
        addThirdPlayer();
        Permanent caller = addCreatureReady(player1, new CallerOfThePack());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent copy = findPermanents(player1, "Caller of the Pack").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(caller);
    }

    @Test
    @DisplayName("Myriad may be declined")
    void myriadMayBeDeclined() {
        addThirdPlayer();
        addCreatureReady(player1, new CallerOfThePack());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Caller of the Pack"))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Myriad creates no copies when there is only one opponent")
    void noCopiesInTwoPlayerGame() {
        Permanent caller = addCreatureReady(player1, new CallerOfThePack());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Caller of the Pack")).containsExactly(caller);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    @DisplayName("Myriad still creates copies when Caller attacks a planeswalker")
    void attackingPlaneswalkerCreatesCopyForOtherOpponent() {
        addThirdPlayer();
        addCreatureReady(player1, new CallerOfThePack());
        Permanent teferi = harness.enterBattlefieldAndReturn(player2, new TeferiTemporalArchmage());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, teferi.getId()));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Caller of the Pack").stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .hasSize(1)
                .allSatisfy(copy -> assertThat(copy.getAttackTarget()).isEqualTo(player3.getId()));
    }

    @Test
    @DisplayName("Myriad allows a copy to attack the other opponent's planeswalker")
    void offersPlaneswalkerAttackChoice() {
        addThirdPlayer();
        addCreatureReady(player1, new CallerOfThePack());
        harness.enterBattlefieldAndReturn(player3, new TeferiTemporalArchmage());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        });
    }

    @Test
    @DisplayName("Myriad finishes choosing opponents before any copies enter")
    void choosesAllCopiesBeforeCreatingThem() {
        addThirdPlayer();
        addOpponent("Dana", "conn-4");
        Permanent caller = addCreatureReady(player1, new CallerOfThePack());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            assertThat(findPermanents(player1, "Caller of the Pack")).containsExactly(caller);

            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Caller of the Pack").stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(2);
    }

    @Test
    @DisplayName("Myriad exile uses the stack at the beginning of end of combat")
    void exileCanBeRespondedToAtEndOfCombat() {
        addThirdPlayer();
        Permanent caller = addCreatureReady(player1, new CallerOfThePack());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });
        Permanent copy = findPermanents(player1, "Caller of the Pack").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();

        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(caller, copy);
        assertThat(gd.stack).hasSize(1);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, this::resolveAllTriggers);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(caller).doesNotContain(copy);
    }

    @Test
    @DisplayName("Every doubled myriad token attacks the other opponent")
    void doubledCopiesAllHaveAttackTargets() {
        addThirdPlayer();
        addCreatureReady(player1, new CallerOfThePack());
        harness.addToBattlefield(player1, new DoublingSeason());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Caller of the Pack").stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .hasSize(2)
                .allSatisfy(copy -> {
                    assertThat(copy.isTapped()).isTrue();
                    assertThat(copy.isAttacking()).isTrue();
                    assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
                });
    }

    private void addThirdPlayer() {
        player3 = addOpponent("Charlie", "conn-3");
    }

    private Player addOpponent(String name, String connectionId) {
        UUID thirdPlayerId = UUID.randomUUID();
        Player opponent = new Player(thirdPlayerId, name);
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add(name);
        gd.playerIdToName.put(thirdPlayerId, name);
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection(connectionId), thirdPlayerId, name);
        return opponent;
    }
}
