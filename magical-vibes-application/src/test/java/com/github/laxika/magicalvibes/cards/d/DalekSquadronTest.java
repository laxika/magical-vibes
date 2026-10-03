package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DalekSquadron.class, JaceBeleren.class})
class DalekSquadronTest extends BaseCardTest {

    @Test
    @DisplayName("Myriad creates a tapped and attacking copy for another opponent")
    void myriadCreatesCopyForAnotherOpponentAndExilesItAtEndOfCombat() {
        Player player3 = addOpponent("Charlie");
        Permanent squadron = addCreatureReady(player1, new DalekSquadron());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackersAt(player2, squadron);
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player3.getId());

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(squadron);
    }

    @Test
    @DisplayName("Myriad may be declined")
    void myriadMayBeDeclined() {
        addOpponent("Charlie");
        Permanent squadron = addCreatureReady(player1, new DalekSquadron());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackersAt(player2, squadron);
            resolveAllTriggers();
        });
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Myriad creates no copies in a two-player game")
    void myriadCreatesNoCopiesWithOnlyOneOpponent() {
        Permanent squadron = addCreatureReady(player1, new DalekSquadron());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackersAt(player2, squadron);
            resolveAllTriggers();
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(squadron);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Myriad allows a separate decision for each other opponent")
    void myriadCanBeAcceptedForOneOpponentAndDeclinedForAnother() {
        Player player3 = addOpponent("Charlie");
        addOpponent("Dana");
        Permanent squadron = addCreatureReady(player1, new DalekSquadron());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackersAt(player2, squadron);
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleMayAbilityChosen(player1, false);
        });
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getAttackTarget()).isEqualTo(player3.getId());
        assertThat(tokens.getFirst().isTapped()).isTrue();
        assertThat(tokens.getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Myriad still creates copies when the original attacks a planeswalker")
    void myriadTriggersWhenAttackingPlaneswalker() {
        Player player3 = addOpponent("Charlie");
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new JaceBeleren());
        Permanent squadron = addCreatureReady(player1, new DalekSquadron());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackersAt(planeswalker.getId(), squadron);
            resolveAllTriggers();
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            harness.handleMayAbilityChosen(player1, true);
        });
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getAttackTarget()).isEqualTo(player3.getId());
    }

    private void declareAttackersAt(Player target, Permanent attacker) {
        declareAttackersAt(target.getId(), attacker);
    }

    private void declareAttackersAt(UUID targetId, Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player1, List.of(attackerIndex),
                java.util.Map.of(attackerIndex, targetId));
    }

    private Player addOpponent(String name) {
        Player opponent = new Player(UUID.randomUUID(), name);
        gd.playerIds.add(opponent.getId());
        gd.orderedPlayerIds.add(opponent.getId());
        gd.playerNames.add(name);
        gd.playerIdToName.put(opponent.getId(), name);
        gd.playerDecks.put(opponent.getId(), new ArrayList<>());
        gd.playerHands.put(opponent.getId(), new ArrayList<>());
        gd.playerGraveyards.put(opponent.getId(), new ArrayList<>());
        gd.playerBattlefields.put(opponent.getId(), new ArrayList<>());
        gd.playerCommandZones.put(opponent.getId(), new ArrayList<>());
        gd.playerManaPools.put(opponent.getId(), new ManaPool());
        gd.playerLifeTotals.put(opponent.getId(), 20);
        return opponent;
    }
}
