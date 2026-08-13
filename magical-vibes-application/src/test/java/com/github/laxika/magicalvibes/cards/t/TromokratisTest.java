package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TromokratisTest extends BaseCardTest {

    @Test
    @DisplayName("Tromokratis has hexproof while it is neither attacking nor blocking")
    void hasHexproofWhileNotInCombat() {
        Permanent tromokratis = addCreatureReady(player1, new Tromokratis());

        assertThat(gqs.hasKeyword(gd, tromokratis, Keyword.HEXPROOF)).isTrue();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, tromokratis.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Tromokratis loses hexproof while attacking or blocking")
    void losesHexproofWhileInCombat() {
        Permanent tromokratis = addCreatureReady(player1, new Tromokratis());
        tromokratis.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, tromokratis, Keyword.HEXPROOF)).isFalse();
        tromokratis.setAttacking(false);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blockingTromokratis = addCreatureReady(player2, new Tromokratis());
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blockingTromokratis),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blockingTromokratis.isBlocking()).isTrue();
        assertThat(gqs.hasKeyword(gd, blockingTromokratis, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("A Tromokratis block must include every defending creature")
    void blockRequiresEveryDefendingCreature() {
        Permanent tromokratis = addAttackingTromokratis();
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        int tromokratisIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tromokratis);
        int firstBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker);
        int secondBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(firstBlockerIndex, tromokratisIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("all creatures defending player controls");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(firstBlockerIndex, tromokratisIndex),
                new BlockerAssignment(secondBlockerIndex, tromokratisIndex)));
        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tromokratis may remain unblocked")
    void mayRemainUnblocked() {
        Permanent tromokratis = addAttackingTromokratis();
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of());

        assertThat(firstBlocker.isBlocking()).isFalse();
        assertThat(secondBlocker.isBlocking()).isFalse();
    }

    private Permanent addAttackingTromokratis() {
        Permanent tromokratis = addCreatureReady(player1, new Tromokratis());
        tromokratis.setAttacking(true);
        return tromokratis;
    }
}
