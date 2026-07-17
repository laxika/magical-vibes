package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeasingerTest extends BaseCardTest {

    @Test
    @DisplayName("{T} gains control of a creature whose controller controls an Island; Seasinger stays tapped")
    void gainsControlWhileTapped() {
        harness.addToBattlefield(player1, new Island());
        Permanent seasinger = addReadySeasinger(player1);

        harness.addToBattlefield(player2, new Island());
        Permanent bears = addReadyCreature(player2);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(seasinger);
        harness.activateAbility(player1, idx, null, bears.getId());
        harness.passBothPriorities();

        // The creature is now controlled by player1, the ability paid {T}.
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(seasinger.isTapped()).isTrue();
        assertThat(gd.newestControlEffectFor(bears.getId())).isNotNull();
        assertThat(gd.newestControlEffectFor(bears.getId()).sourcePermanentId()).isEqualTo(seasinger.getId());
    }

    @Test
    @DisplayName("Cannot target a creature whose controller controls no Island")
    void cannotTargetCreatureWithoutIslandController() {
        harness.addToBattlefield(player1, new Island());
        Permanent seasinger = addReadySeasinger(player1);

        // player2 controls the creature but no Island.
        Permanent bears = addReadyCreature(player2);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(seasinger);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("controls an Island");
    }

    @Test
    @DisplayName("Control is lost when Seasinger untaps during its controller's untap step")
    void controlLostWhenSeasingerUntaps() {
        harness.addToBattlefield(player1, new Island());
        Permanent seasinger = addReadySeasinger(player1);

        harness.addToBattlefield(player2, new Island());
        Permanent bears = addReadyCreature(player2);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(seasinger);
        harness.activateAbility(player1, idx, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bears.getId()));

        // player2's turn: Seasinger stays tapped, so control is retained.
        advanceToNextTurn(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bears.getId()));

        // player1's untap step: choose to untap Seasinger. Control ends immediately.
        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(seasinger.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.controlEffectsFor(bears.getId())).isEmpty();
    }

    @Test
    @DisplayName("Keeping Seasinger tapped retains control across the controller's untap step")
    void keepingTappedRetainsControl() {
        harness.addToBattlefield(player1, new Island());
        Permanent seasinger = addReadySeasinger(player1);

        harness.addToBattlefield(player2, new Island());
        Permanent bears = addReadyCreature(player2);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(seasinger);
        harness.activateAbility(player1, idx, null, bears.getId());
        harness.passBothPriorities();

        advanceToNextTurn(player1); // player2's turn
        // player1's untap step: choose NOT to untap Seasinger. Control persists.
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(seasinger.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Control is lost when Seasinger leaves the battlefield")
    void controlLostWhenSeasingerLeaves() {
        harness.addToBattlefield(player1, new Island());
        Permanent seasinger = addReadySeasinger(player1);

        harness.addToBattlefield(player2, new Island());
        Permanent bears = addReadyCreature(player2);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(seasinger);
        harness.activateAbility(player1, idx, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bears.getId()));

        // Seasinger leaves — the stolen creature reverts to its owner.
        gd.playerBattlefields.get(player1.getId()).remove(seasinger);
        advanceToNextTurn(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Seasinger is sacrificed when its controller controls no Island")
    void sacrificedWhenNoIsland() {
        // No Island for player1 — the state-triggered ability sacrifices Seasinger.
        harness.setHand(player1, List.of(new Seasinger()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → state trigger fires
        harness.passBothPriorities(); // resolve state trigger → sacrificed

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Seasinger"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Seasinger"));
    }

    // ===== Helpers =====

    private Permanent addReadySeasinger(Player player) {
        Permanent perm = new Permanent(new Seasinger());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP -> advanceTurn -> may ability prompt

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
