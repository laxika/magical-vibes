package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hammerhand;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetathranEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Metathran Elite can't be blocked while enchanted")
    void cannotBeBlockedWhileEnchanted() {
        Permanent elite = addAttackingElite();
        Permanent aura = new Permanent(new Hammerhand());
        aura.setAttachedTo(elite.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        Permanent blocker = addReadyBlocker();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(elite)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Metathran Elite can be blocked while not enchanted")
    void canBeBlockedWhileNotEnchanted() {
        Permanent elite = addAttackingElite();
        Permanent blocker = addReadyBlocker();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(elite))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttackingElite() {
        Permanent elite = new Permanent(new MetathranElite());
        elite.setSummoningSick(false);
        elite.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(elite);
        return elite;
    }

    private Permanent addReadyBlocker() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        return blocker;
    }
}
