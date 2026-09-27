package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.MossKami;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KashiTribeWarriors.class, MossKami.class})
class KashiTribeWarriorsTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksBlocker() {
        Permanent warriors = addCreatureReady(player1, new KashiTribeWarriors());
        warriors.setAttacking(true);
        // 5/5 survives the Warriors' 2 damage, so the tap/untap lock is observable.
        Permanent mossKami = addCreatureReady(player2, new MossKami());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(mossKami.isTapped()).isTrue();
        assertThat(mossKami.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);

        assertThat(mossKami.isTapped()).isTrue();
        assertThat(mossKami.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);

        assertThat(mossKami.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tap and untap lock resolve as one triggered ability")
    void combatDamageInstructionsResolveTogether() {
        Permanent warriors = addCreatureReady(player1, new KashiTribeWarriors());
        warriors.setAttacking(true);
        Permanent mossKami = addCreatureReady(player2, new MossKami());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.resolveCombatDamage();
        harness.passBothPriorities();

        assertThat(mossKami.isTapped()).isTrue();
        assertThat(mossKami.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to a player does not tap or lock any creature")
    void unblockedDamageDoesNotTapCreatures() {
        Permanent warriors = addCreatureReady(player1, new KashiTribeWarriors());
        warriors.setAttacking(true);
        Permanent mossKami = addCreatureReady(player2, new MossKami());

        prepareDeclareBlockers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(mossKami.isTapped()).isFalse();
        assertThat(mossKami.getSkipUntapCount()).isZero();
    }
}
