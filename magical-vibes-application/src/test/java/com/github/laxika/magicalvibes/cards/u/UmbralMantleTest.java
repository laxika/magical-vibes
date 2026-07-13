package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UmbralMantleTest extends BaseCardTest {

    // ===== Equip ability ({0}) =====

    @Test
    @DisplayName("Resolving equip ability attaches Umbral Mantle to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent mantle = addMantleReady(player1);
        Permanent creature = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mantle.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Granted activated ability: {3}, {Q}: +2/+2 =====

    @Test
    @DisplayName("Tapped equipped creature can untap and pay {3} to get +2/+2")
    void grantedAbilityGivesBoost() {
        Permanent creature = addReadyCreature(player1);
        creature.tap();
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        // {Q} untaps the creature as part of the cost
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffEndOfTurn() {
        Permanent creature = addReadyCreature(player1);
        creature.tap();
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        advanceToNextTurn(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    // ===== {Q} requires the creature to be tapped =====

    @Test
    @DisplayName("Untapped equipped creature cannot activate the granted ability")
    void cannotActivateWhenUntapped() {
        Permanent creature = addReadyCreature(player1);
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    // ===== Ability is lost when the Equipment leaves =====

    @Test
    @DisplayName("Creature loses granted ability when Umbral Mantle is removed")
    void creatureLosesAbilityWhenRemoved() {
        Permanent creature = addReadyCreature(player1);
        creature.tap();
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gd.playerBattlefields.get(player1.getId()).remove(mantle);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    // ===== Helpers =====

    private Permanent addMantleReady(Player player) {
        Permanent perm = new Permanent(new UmbralMantle());
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

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
