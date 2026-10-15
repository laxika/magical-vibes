package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WarchiefGiant.class)
class WarchiefGiantTest extends BaseCardTest {

    private Player player3;

    @Test
    @DisplayName("Myriad creates a tapped and attacking copy for another opponent")
    void myriadCreatesCopyForAnotherOpponentAndExilesItAtEndOfCombat() {
        addThirdPlayer();
        Permanent giant = addCreatureReady(player1, new WarchiefGiant());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent copy = findPermanents(player1, "Warchief Giant").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(copy.getId())
                        && action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant);
    }

    @Test
    @DisplayName("Myriad may be declined")
    void myriadMayBeDeclined() {
        addThirdPlayer();
        addCreatureReady(player1, new WarchiefGiant());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Warchief Giant"))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Myriad creates no copies in a two-player game")
    void myriadCreatesNoCopiesWithOnlyDefendingOpponent() {
        addCreatureReady(player1, new WarchiefGiant());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Warchief Giant")).hasSize(1);
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Haste allows a newly entered Giant to attack")
    void newlyEnteredGiantCanAttack() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new WarchiefGiant());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(giant.isAttacking()).isTrue();
        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Myriad still creates a copy after the attacking Giant leaves the battlefield")
    void myriadUsesLastKnownInformationAfterGiantLeaves() {
        addThirdPlayer();
        Permanent giant = addCreatureReady(player1, new WarchiefGiant());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, giant));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        assertThat(findPermanents(player1, "Warchief Giant"))
                .singleElement().satisfies(copy -> {
                    assertThat(copy.getCard().isToken()).isTrue();
                    assertThat(copy.isTapped()).isTrue();
                    assertThat(copy.isAttacking()).isTrue();
                    assertThat(copy.getAttackTarget()).isEqualTo(player3.getId());
                });
    }

    private void addThirdPlayer() {
        player3 = addAdditionalPlayer("Charlie", "conn-3");
    }

    @Test
    @DisplayName("A single Myriad resolution exiles all its copies with one delayed trigger")
    void myriadExilesAllCopiesWithOneDelayedTrigger() {
        addThirdPlayer();
        addAdditionalPlayer("Dana", "conn-4");
        addCreatureReady(player1, new WarchiefGiant());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        assertThat(findPermanents(player1, "Warchief Giant")
                .stream().filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(2);

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.END_OF_COMBAT, harness::passBothPriorities);
        assertThat(findPermanents(player1, "Warchief Giant"))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private Player addAdditionalPlayer(String name, String connectionId) {
        UUID thirdPlayerId = UUID.randomUUID();
        Player additionalPlayer = new Player(thirdPlayerId, name);
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
        return additionalPlayer;
    }
}
