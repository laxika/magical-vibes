package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SerratedBiskelion;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForiysianBrigade.class, SerratedBiskelion.class})
class ForiysianBrigadeTest extends BaseCardTest {

    private Permanent addBrigade() {
        return addCreatureReady(player2, new ForiysianBrigade());
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent atk = addCreatureReady(player1, new SerratedBiskelion());
            atk.setAttacking(true);
        }
    }

    @Test
    @DisplayName("Foriysian Brigade can block two attackers")
    void canBlockTwoAttackers() {
        Permanent brigade = addBrigade();
        int idx = gd.playerBattlefields.get(player2.getId()).indexOf(brigade);
        addAttackers(2);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(idx, 0),
                new BlockerAssignment(idx, 1)
        ));

        assertThat(brigade.isBlocking()).isTrue();
        assertThat(brigade.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Foriysian Brigade cannot block three attackers")
    void cannotBlockThreeAttackers() {
        Permanent brigade = addBrigade();
        int idx = gd.playerBattlefields.get(player2.getId()).indexOf(brigade);
        addAttackers(3);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(idx, 0),
                new BlockerAssignment(idx, 1),
                new BlockerAssignment(idx, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Foriysian Brigade does not grant additional blocks to other creatures")
    void doesNotGrantAdditionalBlocksToOthers() {
        addBrigade();

        Permanent bears = addCreatureReady(player2, new SerratedBiskelion());
        int bearIdx = gd.playerBattlefields.get(player2.getId()).indexOf(bears);

        addAttackers(2);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(bearIdx, 0),
                new BlockerAssignment(bearIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Foriysian Brigade can still block a single attacker")
    void canBlockOneAttacker() {
        Permanent brigade = addBrigade();
        int idx = gd.playerBattlefields.get(player2.getId()).indexOf(brigade);
        addAttackers(1);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(idx, 0)));

        assertThat(brigade.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Foriysian Brigade blocking two 2/2s survives and kills one of them")
    void combatDamageWithMultiBlock() {
        Permanent brigade = addBrigade();
        brigade.setBlocking(true);
        brigade.addBlockingTarget(0);
        brigade.addBlockingTarget(1);
        addAttackers(2);

        List<Permanent> attackers = gd.playerBattlefields.get(player1.getId());
        Permanent atk1 = attackers.get(0);
        Permanent atk2 = attackers.get(1);

        resolveCombat();

        // CR 510.1d — a blocker blocking two attackers divides its combat damage among them.
        harness.handleCombatDamageAssigned(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(brigade),
                java.util.Map.of(atk1.getId(), 2, atk2.getId(), 0));

        // 2/4 takes 4 damage total and dies; only the attacker assigned 2 damage dies.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Foriysian Brigade");
    }
}
