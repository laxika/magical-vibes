package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrenchingSteed.class, RhysticCave.class})
class TrenchingSteedTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land gives Trenching Steed +0/+3 until end of turn")
    void sacrificeLandBoostsSelf() {
        Permanent steed = addCreatureReady(player1, new TrenchingSteed());
        harness.addToBattlefield(player1, new RhysticCave());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(steed.getPowerModifier()).isEqualTo(0);
        assertThat(steed.getToughnessModifier()).isEqualTo(3);
        assertThat(countPermanents(player1, "Rhystic Cave")).isZero();
    }

    @Test
    @DisplayName("With multiple lands, prompts which land to sacrifice")
    void multipleLandsPromptChoice() {
        addCreatureReady(player1, new TrenchingSteed());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstLand.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rhystic Cave"))
                .containsExactly(secondLand);
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void cannotActivateWithoutLand() {
        addCreatureReady(player1, new TrenchingSteed());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's land")
    void cannotActivateWithOnlyOpponentsLand() {
        addCreatureReady(player1, new TrenchingSteed());
        harness.addToBattlefield(player2, new RhysticCave());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");
    }

    @Test
    @DisplayName("Repeated activations stack their toughness bonuses")
    void repeatedActivationsStack() {
        Permanent steed = addCreatureReady(player1, new TrenchingSteed());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        harness.addToBattlefield(player1, new RhysticCave());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstLand.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(steed.getPowerModifier()).isZero();
        assertThat(steed.getToughnessModifier()).isEqualTo(6);
        assertThat(countPermanents(player1, "Rhystic Cave")).isZero();
    }

    @Test
    @DisplayName("The toughness boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent steed = addCreatureReady(player1, new TrenchingSteed());
        harness.addToBattlefield(player1, new RhysticCave());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(steed.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(steed.getPowerModifier()).isEqualTo(0);
        assertThat(steed.getToughnessModifier()).isEqualTo(0);
    }
}
