package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerfolkRaiders.class, Island.class})
class MerfolkRaidersTest extends BaseCardTest {

    @Test
    @DisplayName("Merfolk Raiders phases out during its controller's untap step and phases back in the next one")
    void phasesOutAndInOnControllersUntapSteps() {
        Permanent raiders = addCreatureReady(player1, new MerfolkRaiders());

        advanceTurn(player2); // player2's untap step — nothing happens to player1's permanents
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(raiders);

        advanceTurn(player1); // player1's untap step — Merfolk Raiders phases out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(raiders);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(raiders);

        advanceTurn(player2); // player2's untap step — still phased out
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(raiders);

        advanceTurn(player1); // player1's untap step — phases back in
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(raiders);
    }

    @Test
    @DisplayName("Islandwalk prevents blocking while the defending player controls an Island")
    void islandwalkPreventsBlockingWithIsland() {
        Permanent blocker = addCreatureReady(player2, new MerfolkRaiders());
        harness.addToBattlefield(player2, new Island());
        Permanent attacker = addAttackingRaiders();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlocker(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Merfolk Raiders can't be blocked (islandwalk)");
    }

    @Test
    @DisplayName("Islandwalk allows blocking when the defending player controls no Island")
    void islandwalkAllowsBlockingWithoutIsland() {
        Permanent blocker = addCreatureReady(player2, new MerfolkRaiders());
        Permanent attacker = addAttackingRaiders();

        prepareDeclareBlockers();
        declareBlocker(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttackingRaiders() {
        Permanent attacker = addCreatureReady(player1, new MerfolkRaiders());
        attacker.setAttacking(true);
        return attacker;
    }

    private void declareBlocker(Permanent blocker, Permanent attacker) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }

    private void advanceTurn(Player activePlayer) {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(activePlayer, TurnStep.UPKEEP);
    }
}
