package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MatsuTribeDecoy.class, KamiOfOldStone.class})
class MatsuTribeDecoyTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability forces the target to block the Decoy")
    void abilityForcesTargetToBlock() {
        Permanent decoy = addCreatureReady(player1, new MatsuTribeDecoy());
        Permanent blocker = addCreatureReady(player2, new KamiOfOldStone());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).contains(decoy.getId());

        decoy.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Ability cannot be activated without enough mana")
    void abilityNeedsMana() {
        addCreatureReady(player1, new MatsuTribeDecoy());
        Permanent blocker = addCreatureReady(player2, new KamiOfOldStone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksBlocker() {
        Permanent decoy = addCreatureReady(player1, new MatsuTribeDecoy());
        decoy.setAttacking(true);
        // 1/7 survives the Decoy's 1 damage, so the tap/untap lock is observable.
        Permanent blocker = addCreatureReady(player2, new KamiOfOldStone());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(blocker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Combat damage to a player does not tap or lock any creature")
    void unblockedDamageDoesNotTapCreatures() {
        Permanent decoy = addCreatureReady(player1, new MatsuTribeDecoy());
        decoy.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new KamiOfOldStone());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(blocker.isTapped()).isFalse();
        assertThat(blocker.getSkipUntapCount()).isZero();
    }
}
