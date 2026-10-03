package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BelbesArmor.class, BelbesPercher.class, BelbesPortal.class})
class BelbesArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -X/+X until end of turn")
    void targetCreatureGetsMinusXPlusX() {
        Permanent armor = addReadyArmor(player1);
        Permanent creature = addCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(0);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
        assertThat(armor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("X may be zero")
    void zeroXLeavesCreatureStatsUnchanged() {
        Permanent armor = addReadyArmor(player1);
        Permanent creature = addCreature(player1);

        harness.activateAbility(player1, 0, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(armor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The -X/+X wears off at cleanup")
    void boostWearsOffAtCleanup() {
        addReadyArmor(player1);
        Permanent creature = addCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyArmor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent portal = harness.addToBattlefieldAndReturn(player2, new BelbesPortal());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, portal.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void canActivateOnTheTurnArmorEntersAndReducePowerBelowZero() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new BelbesArmor());
        Permanent creature = addCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 4, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(-2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(6);
        assertThat(armor.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void abilityResolvesAfterArmorLeavesBattlefield() {
        Permanent armor = addReadyArmor(player1);
        Permanent creature = addCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(armor);
        gd.playerGraveyards.get(player1.getId()).add(armor.getCard());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(-1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void cannotActivateWithInsufficientMana() {
        Permanent armor = addReadyArmor(player1);
        Permanent creature = addCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(armor.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedArmorCannotActivateAgain() {
        Permanent armor = addReadyArmor(player1);
        Permanent creature = addCreature(player2);
        armor.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyArmor(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BelbesArmor());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new BelbesPercher());
    }
}
