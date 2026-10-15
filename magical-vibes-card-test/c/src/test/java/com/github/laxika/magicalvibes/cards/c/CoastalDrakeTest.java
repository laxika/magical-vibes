package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KavuGlider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoastalDrake.class, KavuGlider.class})
class CoastalDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Ability returns a target Kavu to its owner's hand")
    void returnsTargetKavuToOwnersHand() {
        Permanent drake = addReadyDrake(player1);
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuGlider());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, kavu.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kavu Glider");
        harness.assertInHand(player2, "Kavu Glider");
        assertThat(drake.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot target a non-Kavu creature")
    void cannotTargetNonKavuCreature() {
        addReadyDrake(player1);
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new CoastalDrake());
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, drake.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Kavu creature");
    }

    @Test
    void canReturnYourOwnKavu() {
        addReadyDrake(player1);
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new KavuGlider());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, kavu.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kavu Glider");
        harness.assertInHand(player1, "Kavu Glider");
    }

    @Test
    void returnsStolenKavuToOwnerRatherThanController() {
        addReadyDrake(player1);
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new KavuGlider());
        gd.stolenCreatures.put(kavu.getId(), player2.getId());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, kavu.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kavu Glider");
        harness.assertInHand(player2, "Kavu Glider");
        harness.assertNotInHand(player1, "Kavu Glider");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new CoastalDrake());
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuGlider());
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kavu.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertOnBattlefield(player2, "Kavu Glider");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent drake = addReadyDrake(player1);
        drake.tap();
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuGlider());
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kavu.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayBlueCostWithOnlyColorlessMana() {
        Permanent drake = addReadyDrake(player1);
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuGlider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kavu.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(drake.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterDrakeLeavesBattlefield() {
        Permanent drake = addReadyDrake(player1);
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuGlider());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, kavu.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, drake);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kavu Glider");
        harness.assertInHand(player2, "Kavu Glider");
        harness.assertInHand(player1, "Coastal Drake");
    }

    private Permanent addReadyDrake(Player player) {
        return addCreatureReady(player, new CoastalDrake());
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
