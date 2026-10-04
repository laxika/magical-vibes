package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowstoneWall.class})
class FlowstoneWallTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +1/-1")
    void activatingAbilityBoosts() {
        Permanent wall = addCreatureReady(player1, new FlowstoneWall());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(1);
        assertThat(wall.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Can activate multiple times — each gives +1/-1")
    void canActivateMultipleTimes() {
        Permanent wall = addCreatureReady(player1, new FlowstoneWall());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(wall.getPowerModifier()).isEqualTo(3);
        assertThat(wall.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("Ability requires {R} mana to activate")
    void abilityRequiresRedMana() {
        addCreatureReady(player1, new FlowstoneWall());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Defender prevents the wall from attacking")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new FlowstoneWall());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent wall = addCreatureReady(player1, new FlowstoneWall());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(0);
        assertThat(wall.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated while summoning sick and tapped")
    void canActivateWhileSummoningSickAndTapped() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new FlowstoneWall());
        wall.setSummoningSick(true);
        wall.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(1);
        assertThat(wall.getToughnessModifier()).isEqualTo(-1);
        assertThat(wall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Six activations put the wall into the graveyard at zero toughness")
    void diesAtZeroToughness() {
        addCreatureReady(player1, new FlowstoneWall());
        harness.addMana(player1, ManaColor.RED, 6);

        for (int i = 0; i < 5; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        harness.assertOnBattlefield(player1, "Flowstone Wall");
        harness.assertNotInGraveyard(player1, "Flowstone Wall");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flowstone Wall");
        harness.assertInGraveyard(player1, "Flowstone Wall");
    }

    @Test
    @DisplayName("Ability changes only its source and waits for resolution")
    void boostsOnlyItsSourceOnResolution() {
        Permanent otherWall = addCreatureReady(player1, new FlowstoneWall());
        Permanent source = addCreatureReady(player1, new FlowstoneWall());
        Permanent opposingWall = addCreatureReady(player2, new FlowstoneWall());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();

        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isEqualTo(-1);
        assertThat(otherWall.getPowerModifier()).isZero();
        assertThat(otherWall.getToughnessModifier()).isZero();
        assertThat(opposingWall.getPowerModifier()).isZero();
        assertThat(opposingWall.getToughnessModifier()).isZero();
    }
}
