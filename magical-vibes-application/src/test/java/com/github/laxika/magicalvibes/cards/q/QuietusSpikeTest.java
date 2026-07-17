package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuietusSpikeTest extends BaseCardTest {

    // ===== Deathtouch grant =====

    @Test
    @DisplayName("Equipped creature has deathtouch")
    void equippedCreatureHasDeathtouch() {
        Permanent creature = addReadyCreature(player1);
        Permanent spike = addSpikeReady(player1);
        spike.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Creature loses deathtouch when Quietus Spike is removed")
    void creatureLosesDeathtouchWhenRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent spike = addSpikeReady(player1);
        spike.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(spike);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    // ===== Combat damage trigger: lose half life =====

    @Test
    @DisplayName("Damaged player loses half their life (even total after combat damage)")
    void damagedPlayerLosesHalfLifeEven() {
        harness.setLife(player2, 22);
        Permanent creature = addReadyCreature(player1);
        Permanent spike = addSpikeReady(player1);
        spike.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        // GrizzlyBears deals 2 combat damage first: 22 -> 20.
        // Trigger then makes them lose half of 20 = 10: 20 -> 10.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Half life is rounded up (odd total after combat damage)")
    void halfLifeRoundedUp() {
        harness.setLife(player2, 23);
        Permanent creature = addReadyCreature(player1);
        Permanent spike = addSpikeReady(player1);
        spike.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        // 2 combat damage: 23 -> 21. Half of 21 rounded up = 11: 21 -> 10.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    // ===== No trigger without combat damage to a player =====

    @Test
    @DisplayName("No life loss when equipped creature is blocked and deals no player damage")
    void noLifeLossWhenBlocked() {
        harness.setLife(player2, 22);
        Permanent creature = addReadyCreature(player1);
        Permanent spike = addSpikeReady(player1);
        spike.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Equipped creature dealt no combat damage to a player -> no lose-half trigger.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    // ===== Helpers =====

    private Permanent addSpikeReady(Player player) {
        Permanent perm = new Permanent(new QuietusSpike());
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

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
