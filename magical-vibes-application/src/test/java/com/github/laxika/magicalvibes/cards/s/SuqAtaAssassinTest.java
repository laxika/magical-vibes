package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Breezekeeper;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.cards.u.UrborgMindsucker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuqAtaAssassin.class, Breezekeeper.class, PhyrexianWalker.class, UrborgMindsucker.class})
class SuqAtaAssassinTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new SuqAtaAssassin());
        attacker.setAttacking(true);
        return attacker;
    }

    @Test
    @DisplayName("Unblocked attacker gives the defending player a poison counter")
    void unblockedGivesPoison() {
        addCreatureReady(player1, new SuqAtaAssassin());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Blocked attacker gives no poison counter")
    void blockedNoPoison() {
        // Phyrexian Walker is an artifact creature, so it can block through Fear.
        addCreatureReady(player2, new PhyrexianWalker());

        addAttacker();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking")
    void nonblackNonartifactCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new Breezekeeper());
        addAttacker();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Fear allows a black creature to block")
    void blackCreatureCanBlock() {
        Permanent blocker = addCreatureReady(player2, new UrborgMindsucker());
        addAttacker();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Poison goes to the defending player when player two attacks")
    void playerTwoAttacks() {
        addCreatureReady(player2, new SuqAtaAssassin());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Each unblocked Assassin gives a poison counter")
    void multipleUnblockedAssassins() {
        addCreatureReady(player1, new SuqAtaAssassin());
        addCreatureReady(player1, new SuqAtaAssassin());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Only the unblocked Assassin gives a poison counter")
    void oneOfTwoAssassinsBlocked() {
        addCreatureReady(player1, new SuqAtaAssassin());
        addCreatureReady(player1, new SuqAtaAssassin());
        addCreatureReady(player2, new PhyrexianWalker());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Poison is given before combat damage and does not replace that damage")
    void poisonBeforeCombatDamage() {
        addCreatureReady(player1, new SuqAtaAssassin());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of());
            resolveAllTriggers();
        });

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        harness.assertLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
}
