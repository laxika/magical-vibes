package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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

@CardUsed(GoldlustTriad.class)
class GoldlustTriadTest extends BaseCardTest {

    private Player player3;

    @Test
    @DisplayName("Myriad creates a tapped and attacking copy for another opponent")
    void myriadCreatesCopyAndExilesItAtEndOfCombat() {
        addThirdPlayer();
        Permanent triad = addCreatureReady(player1, new GoldlustTriad());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent copy = findPermanents(player1, "Goldlust Triad").stream()
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
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(triad);
    }

    @Test
    @DisplayName("Myriad may be declined")
    void myriadMayBeDeclined() {
        addThirdPlayer();
        addCreatureReady(player1, new GoldlustTriad());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Goldlust Triad"))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Creates a Treasure token when it deals combat damage to a player")
    void createsTreasureTokenOnCombatDamageToPlayer() {
        addCreatureReady(player1, new GoldlustTriad()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Myriad creates no copies in a two-player game")
    void myriadDoesNothingWithoutAnotherOpponent() {
        addCreatureReady(player1, new GoldlustTriad());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Goldlust Triad")).hasSize(1);
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Both the original and its myriad copy create Treasure on combat damage")
    void myriadCopyRetainsCombatDamageTrigger() {
        addThirdPlayer();
        addCreatureReady(player1, new GoldlustTriad());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        assertThat(findPermanents(player1, "Goldlust Triad")).hasSize(2);
        assertThat(gd.stack).isEmpty();
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        harness.assertLife(player3, 16);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not create Treasure")
    void creatureCombatDamageDoesNotCreateTreasure() {
        addCreatureReady(player1, new GoldlustTriad());
        addCreatureReady(player2, new GoldlustTriad());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    private void addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
    }
}
