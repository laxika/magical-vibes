package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowstoneGiant.class})
class FlowstoneGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +2/-2 to Flowstone Giant")
    void resolvingAbilityBoosts() {
        Permanent giant = addCreatureReady(player1, new FlowstoneGiant());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(giant.getEffectivePower()).isEqualTo(5);
        assertThat(giant.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating twice kills Flowstone Giant via state-based actions")
    void toughnessDropsToZeroAndItDies() {
        addCreatureReady(player1, new FlowstoneGiant());
        harness.addMana(player1, ManaColor.RED, 2);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent giant = addCreatureReady(player1, new FlowstoneGiant());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(giant.getEffectivePower()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(giant.getEffectivePower()).isEqualTo(3);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new FlowstoneGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Giant can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new FlowstoneGiant());
        giant.setSummoningSick(true);
        giant.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(giant.getEffectivePower()).isEqualTo(5);
        assertThat(giant.getEffectiveToughness()).isEqualTo(1);
        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability boosts only its source, not other Flowstone Giants")
    void boostsOnlyItsSource() {
        Permanent source = addCreatureReady(player1, new FlowstoneGiant());
        Permanent other = addCreatureReady(player1, new FlowstoneGiant());
        Permanent opponent = addCreatureReady(player2, new FlowstoneGiant());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(5);
        assertThat(source.getEffectiveToughness()).isEqualTo(1);
        assertThat(other.getEffectivePower()).isEqualTo(3);
        assertThat(other.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponent.getEffectivePower()).isEqualTo(3);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Queued activations resolve harmlessly after their source dies")
    void queuedAbilityDoesNotBoostAnotherGiantAfterSourceDies() {
        addCreatureReady(player1, new FlowstoneGiant());
        Permanent other = addCreatureReady(player1, new FlowstoneGiant());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
        }
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
        harness.assertInGraveyard(player1, "Flowstone Giant");
        assertThat(other.getEffectivePower()).isEqualTo(3);
        assertThat(other.getEffectiveToughness()).isEqualTo(3);
    }

}
