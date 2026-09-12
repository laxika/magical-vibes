package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MetathranElite.class, MarkOfFury.class, MetathranSoldier.class})
class MetathranEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Metathran Elite can't be blocked while enchanted")
    void cannotBeBlockedWhileEnchanted() {
        Permanent elite = addAttackingElite();
        Permanent aura = new Permanent(new MarkOfFury());
        aura.setAttachedTo(elite.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        Permanent blocker = addCreatureReady(player2, new MetathranSoldier());

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
        Permanent blocker = addCreatureReady(player2, new MetathranSoldier());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(elite))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Metathran Elite can be blocked when an Aura enchants another creature")
    void canBeBlockedWhenAnotherCreatureIsEnchanted() {
        Permanent elite = addAttackingElite();
        Permanent blocker = addCreatureReady(player2, new MetathranSoldier());
        Permanent aura = new Permanent(new MarkOfFury());
        aura.setAttachedTo(blocker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(elite))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttackingElite() {
        Permanent elite = addCreatureReady(player1, new MetathranElite());
        elite.setAttacking(true);
        return elite;
    }
}
