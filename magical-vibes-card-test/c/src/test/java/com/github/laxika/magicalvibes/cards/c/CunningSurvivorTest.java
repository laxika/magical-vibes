package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.StripedRiverwinder;
import com.github.laxika.magicalvibes.cards.t.TormentOfScarabs;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CunningSurvivor.class, StripedRiverwinder.class, TormentOfScarabs.class})
class CunningSurvivorTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card gives +1/+0 and makes this creature unblockable")
    void cyclingBoostsAndUnblockable() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CunningSurvivor());
        harness.setHand(player1, List.of(new StripedRiverwinder()));
        harness.setLibrary(player1, List.of(new CunningSurvivor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // resolve the trigger

        assertThat(survivor.getPowerModifier()).isEqualTo(1);
        assertThat(survivor.getToughnessModifier()).isEqualTo(0);
        assertThat(survivor.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Each discard stacks another +1/+0")
    void discardsStackPower() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CunningSurvivor());
        harness.setHand(player1, List.of(new StripedRiverwinder(), new StripedRiverwinder()));
        harness.setLibrary(player1, List.of(new CunningSurvivor(), new CunningSurvivor()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(survivor.getPowerModifier()).isEqualTo(2);
        assertThat(survivor.getToughnessModifier()).isEqualTo(0);
        assertThat(survivor.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The boost and unblockable wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CunningSurvivor());
        harness.setHand(player1, List.of(new StripedRiverwinder()));
        harness.setLibrary(player1, List.of(new CunningSurvivor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(survivor.getPowerModifier()).isEqualTo(1);
        assertThat(survivor.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(survivor.getPowerModifier()).isEqualTo(0);
        assertThat(survivor.getToughnessModifier()).isEqualTo(0);
        assertThat(survivor.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cycling waits for the trigger to resolve and triggers only once")
    void cyclingTriggerUsesStack() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CunningSurvivor());
        harness.setHand(player1, List.of(new StripedRiverwinder()));
        harness.setLibrary(player1, List.of(new CunningSurvivor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(survivor.getPowerModifier()).isZero();
        assertThat(survivor.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();
        assertThat(survivor.getPowerModifier()).isEqualTo(1);
        assertThat(survivor.isCantBeBlocked()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(survivor.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Survivor receives its own boost from one discard")
    void multipleSurvivorsTriggerIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CunningSurvivor());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CunningSurvivor());
        harness.setHand(player1, List.of(new StripedRiverwinder()));
        harness.setLibrary(player1, List.of(new CunningSurvivor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        for (Permanent survivor : List.of(first, second)) {
            assertThat(survivor.getPowerModifier()).isEqualTo(1);
            assertThat(survivor.getToughnessModifier()).isZero();
            assertThat(survivor.isCantBeBlocked()).isTrue();
        }
    }

    @Test
    @DisplayName("An opponent cycling a card does not trigger Survivor")
    void opponentCyclingDoesNotTrigger() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CunningSurvivor());
        harness.setHand(player2, List.of(new StripedRiverwinder()));
        harness.setLibrary(player2, List.of(new CunningSurvivor()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(survivor.getPowerModifier()).isZero();
        assertThat(survivor.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Discarding without cycling also boosts Survivor and prevents blocking")
    void ordinaryDiscardTriggers() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CunningSurvivor());
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new TormentOfScarabs());
        curse.setAttachedTo(player1.getId());
        harness.setHand(player1, List.of(new StripedRiverwinder()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Striped Riverwinder");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(survivor.getPowerModifier()).isEqualTo(1);
        assertThat(survivor.getToughnessModifier()).isZero();
        assertThat(survivor.isCantBeBlocked()).isTrue();
    }

    private Permanent getSurvivor() {
        return findPermanent(player1, "Cunning Survivor");
    }
}
