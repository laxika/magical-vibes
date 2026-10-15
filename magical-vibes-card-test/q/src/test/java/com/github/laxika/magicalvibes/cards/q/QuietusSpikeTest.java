package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuietusSpike.class, CylianElf.class})
class QuietusSpikeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has deathtouch")
    void equippedCreatureHasDeathtouch() {
        Permanent creature = addCreatureReady(player1, new CylianElf());
        Permanent spike = addSpikeReady(player1);
        spike.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Creature loses deathtouch when Quietus Spike is removed")
    void creatureLosesDeathtouchWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new CylianElf());
        Permanent spike = addSpikeReady(player1);
        spike.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(spike);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Damaged player loses half their life (even total after combat damage)")
    void damagedPlayerLosesHalfLifeEven() {
        harness.setLife(player2, 22);
        Permanent creature = addCreatureReady(player1, new CylianElf());
        Permanent spike = addSpikeReady(player1);
        spike.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        // CylianElf deals 2 combat damage first: 22 -> 20.
        // Trigger then makes them lose half of 20 = 10: 20 -> 10.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Half life is rounded up (odd total after combat damage)")
    void halfLifeRoundedUp() {
        harness.setLife(player2, 23);
        Permanent creature = addCreatureReady(player1, new CylianElf());
        Permanent spike = addSpikeReady(player1);
        spike.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        // 2 combat damage: 23 -> 21. Half of 21 rounded up = 11: 21 -> 10.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("No life loss when equipped creature is blocked and deals no player damage")
    void noLifeLossWhenBlocked() {
        harness.setLife(player2, 22);
        Permanent creature = addCreatureReady(player1, new CylianElf());
        Permanent spike = addSpikeReady(player1);
        spike.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CylianElf());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Equipped creature dealt no combat damage to a player -> no lose-half trigger.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Equip attaches Spike and moves deathtouch to the new creature")
    void equipMovesDeathtouch() {
        Permanent spike = addSpikeReady(player1);
        Permanent first = addCreatureReady(player1, new CylianElf());
        Permanent second = addCreatureReady(player1, new CylianElf());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, first.getId());
        resolveAllTriggers();
        assertThat(spike.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DEATHTOUCH)).isFalse();

        harness.activateAbility(player1, 0, null, second.getId());
        resolveAllTriggers();
        assertThat(spike.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Life loss uses the player's life total when the trigger resolves")
    void lifeTotalIsEvaluatedAtResolution() {
        Permanent creature = addCreatureReady(player1, new CylianElf());
        addSpikeReady(player1).setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setLife(player2, 22);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player2, 31);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two Spikes halve the remaining life total separately")
    void multipleSpikesResolveSeparately() {
        Permanent creature = addCreatureReady(player1, new CylianElf());
        addSpikeReady(player1).setAttachedTo(creature.getId());
        addSpikeReady(player1).setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setLife(player2, 23);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("Removing Spike after combat damage does not stop its trigger")
    void triggerSurvivesEquipmentRemoval() {
        Permanent creature = addCreatureReady(player1, new CylianElf());
        Permanent spike = addSpikeReady(player1);
        spike.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setLife(player2, 22);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(spike);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Unattached Spike does not trigger for an attacking creature")
    void unattachedSpikeDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new CylianElf());
        addSpikeReady(player1);
        creature.setAttacking(true);
        harness.setLife(player2, 22);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent addSpikeReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new QuietusSpike());
    }
}
