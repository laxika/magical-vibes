package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.r.RomanticRendezvous;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HobgoblinMantledMarauder.class, Censor.class, RomanticRendezvous.class})
class HobgoblinMantledMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card gives Hobgoblin +2/+0 until end of turn")
    void discardingCardBoostsSelf() {
        Permanent hobgoblin = addHobgoblin();
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hobgoblin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each discarded card adds another +2/+0")
    void discardsStack() {
        Permanent hobgoblin = addHobgoblin();
        harness.setHand(player1, List.of(new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new Censor(), new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hobgoblin)).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent hobgoblin = addHobgoblin();
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, hobgoblin)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hobgoblin)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's discard boosts only their own Hobgoblin")
    void opponentDiscardBoostsOnlyTheirSource() {
        Permanent ownHobgoblin = addHobgoblin();
        Permanent opposingHobgoblin = harness.addToBattlefieldAndReturn(
                player2, new HobgoblinMantledMarauder());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new Censor()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownHobgoblin)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingHobgoblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingHobgoblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Discarding pays cycling's cost but the boost waits for its trigger to resolve")
    void boostWaitsForTriggerResolution() {
        Permanent hobgoblin = addHobgoblin();
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Censor");
        assertThat(gqs.getEffectivePower(gd, hobgoblin)).isEqualTo(1);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hobgoblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hobgoblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("A discard caused by a resolving spell also boosts Hobgoblin")
    void spellEffectDiscardBoostsSelf() {
        Permanent hobgoblin = addHobgoblin();
        harness.setHand(player1, List.of(new RomanticRendezvous(), new RomanticRendezvous()));
        harness.setLibrary(player1, List.of(new RomanticRendezvous(), new RomanticRendezvous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hobgoblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hobgoblin)).isEqualTo(2);
    }

    private Permanent addHobgoblin() {
        return harness.addToBattlefieldAndReturn(player1, new HobgoblinMantledMarauder());
    }
}
