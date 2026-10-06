package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavineRaider.class})
class RavineRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives Ravine Raider +1/+1")
    void activatingAbilityBoostsPowerAndToughness() {
        Permanent raider = addCreatureReady(player1, new RavineRaider());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(raider.getPowerModifier()).isEqualTo(1);
        assertThat(raider.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated multiple times")
    void canActivateMultipleTimes() {
        Permanent raider = addCreatureReady(player1, new RavineRaider());
        harness.addMana(player1, ManaColor.BLACK, 4);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(raider.getPowerModifier()).isEqualTo(2);
        assertThat(raider.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent raider = addCreatureReady(player1, new RavineRaider());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(raider.getPowerModifier()).isEqualTo(1);
        assertThat(raider.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(raider.getPowerModifier()).isEqualTo(0);
        assertThat(raider.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new RavineRaider());
        addCreatureReady(player2, new RavineRaider());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new RavineRaider());
        Permanent firstBlocker = addCreatureReady(player2, new RavineRaider());
        Permanent secondBlocker = addCreatureReady(player2, new RavineRaider());

        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0))));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    void tappedSummoningSickRaiderCanActivate() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new RavineRaider());
        raider.setSummoningSick(true);
        raider.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(raider.getPowerModifier()).isZero();
        assertThat(raider.getToughnessModifier()).isZero();
        harness.passBothPriorities();

        assertThat(raider.getPowerModifier()).isEqualTo(1);
        assertThat(raider.getToughnessModifier()).isEqualTo(1);
        assertThat(raider.isTapped()).isTrue();
    }

    @Test
    void activationBoostsOnlyItsSource() {
        Permanent firstRaider = addCreatureReady(player1, new RavineRaider());
        Permanent secondRaider = addCreatureReady(player1, new RavineRaider());
        Permanent opposingRaider = addCreatureReady(player2, new RavineRaider());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(firstRaider.getPowerModifier()).isZero();
        assertThat(firstRaider.getToughnessModifier()).isZero();
        assertThat(secondRaider.getPowerModifier()).isEqualTo(1);
        assertThat(secondRaider.getToughnessModifier()).isEqualTo(1);
        assertThat(opposingRaider.getPowerModifier()).isZero();
        assertThat(opposingRaider.getToughnessModifier()).isZero();
    }
}
