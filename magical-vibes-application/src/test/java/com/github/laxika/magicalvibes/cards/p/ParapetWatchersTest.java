package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ParapetWatchers.class)
class ParapetWatchersTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability with white mana gives +0/+1")
    void activatingWithWhiteBoostsToughness() {
        Permanent watchers = addCreatureReady(player1, new ParapetWatchers());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(watchers.getPowerModifier()).isEqualTo(0);
        assertThat(watchers.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can also be paid with blue mana (hybrid)")
    void activatingWithBlueBoostsToughness() {
        Permanent watchers = addCreatureReady(player1, new ParapetWatchers());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(watchers.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate multiple times — each gives +0/+1")
    void canActivateMultipleTimes() {
        Permanent watchers = addCreatureReady(player1, new ParapetWatchers());
        harness.addMana(player1, ManaColor.BLUE, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(watchers.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent watchers = addCreatureReady(player1, new ParapetWatchers());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(watchers.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(watchers.getPowerModifier()).isEqualTo(0);
        assertThat(watchers.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate without white or blue mana")
    void cannotActivateWithoutHybridMana() {
        addCreatureReady(player1, new ParapetWatchers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Stacked activations boost only their source when each resolves")
    void stackedActivationsBoostOnlyTheirSource() {
        Permanent watchers = addCreatureReady(player1, new ParapetWatchers());
        Permanent other = addCreatureReady(player1, new ParapetWatchers());
        Permanent opponent = addCreatureReady(player2, new ParapetWatchers());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(watchers.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(watchers.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(watchers.getPowerModifier()).isZero();
        assertThat(watchers.getToughnessModifier()).isEqualTo(2);
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent watchers = harness.addToBattlefieldAndReturn(player1, new ParapetWatchers());
        watchers.setSummoningSick(true);
        watchers.setTapped(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(watchers.getToughnessModifier()).isEqualTo(1);
        assertThat(watchers.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other mana colors and colorless cannot pay the hybrid cost")
    void cannotPayWithOtherMana() {
        Permanent watchers = addCreatureReady(player1, new ParapetWatchers());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(watchers.getToughnessModifier()).isZero();
    }
}
