package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KavuClimber;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoastalDrake.class, KavuClimber.class, GrizzlyBears.class})
class CoastalDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Ability returns a target Kavu to its owner's hand")
    void returnsTargetKavuToOwnersHand() {
        Permanent drake = addReadyDrake(player1);
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuClimber());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, kavu.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kavu Climber");
        harness.assertInHand(player2, "Kavu Climber");
        assertThat(drake.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot target a non-Kavu creature")
    void cannotTargetNonKavuCreature() {
        addReadyDrake(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Kavu creature");
    }

    private Permanent addReadyDrake(Player player) {
        Permanent drake = harness.addToBattlefieldAndReturn(player, new CoastalDrake());
        drake.setSummoningSick(false);
        return drake;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
