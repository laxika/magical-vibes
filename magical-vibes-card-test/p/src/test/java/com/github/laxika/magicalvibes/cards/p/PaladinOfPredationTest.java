package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ContagiousVorrac;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PaladinOfPredation.class, PredationSteward.class, ContagiousVorrac.class})
class PaladinOfPredationTest extends BaseCardTest {

    @Test
    @DisplayName("Paladin of Predation can't be blocked by a creature with power 2 or less")
    void cannotBeBlockedByLowPowerCreature() {
        Permanent blocker = addCreatureReady(player2, new PredationSteward());
        Permanent paladin = addCreatureReady(player1, new PaladinOfPredation());
        paladin.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(paladin);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paladin of Predation can be blocked by a creature with power 3 or greater")
    void canBeBlockedByHighPowerCreature() {
        Permanent blocker = addCreatureReady(player2, new ContagiousVorrac());
        Permanent paladin = addCreatureReady(player1, new PaladinOfPredation());
        paladin.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(paladin);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void boostedTwoPowerCreatureCanBlock() {
        Permanent blocker = addCreatureReady(player2, new PredationSteward());
        blocker.setPowerModifier(1);
        Permanent paladin = addCreatureReady(player1, new PaladinOfPredation());
        paladin.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void reducedThreePowerCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new ContagiousVorrac());
        blocker.setPowerModifier(-1);
        Permanent paladin = addCreatureReady(player1, new PaladinOfPredation());
        paladin.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 6})
    void combatDamageGivesSixPoisonRegardlessOfDamageAmount(int power) {
        harness.setLife(player2, 20);
        Permanent paladin = addCreatureReady(player1, new PaladinOfPredation());
        paladin.setPowerModifier(power - 6);
        paladin.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 20 - power);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(6);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void zeroPowerDoesNotGivePoisonCounters() {
        harness.setLife(player2, 20);
        Permanent paladin = addCreatureReady(player1, new PaladinOfPredation());
        paladin.setPowerModifier(-6);
        paladin.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void blockedPaladinDoesNotGivePoisonCounters() {
        harness.setLife(player2, 20);
        addCreatureReady(player2, new ContagiousVorrac());
        Permanent paladin = addCreatureReady(player1, new PaladinOfPredation());
        paladin.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInGraveyard(player2, "Contagious Vorrac");
        harness.assertOnBattlefield(player1, "Paladin of Predation");
    }
}
