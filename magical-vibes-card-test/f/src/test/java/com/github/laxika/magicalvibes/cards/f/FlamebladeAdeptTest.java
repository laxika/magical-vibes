package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.m.MiasmicMummy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamebladeAdept.class, Censor.class, MiasmicMummy.class})
class FlamebladeAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card gives this creature +1/+0")
    void cyclingBoostsSelf() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new FlamebladeAdept());
        // Cycling is a discard (CR 702.29a), so cycling Censor triggers the +1/+0.
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // resolve the boost trigger

        assertThat(adept.getPowerModifier()).isEqualTo(1);
        assertThat(adept.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Each discard stacks another +1/+0")
    void discardsStack() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new FlamebladeAdept());
        harness.setHand(player1, List.of(new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new Censor(), new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(adept.getPowerModifier()).isEqualTo(2);
        assertThat(adept.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new FlamebladeAdept());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(adept.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(adept.getPowerModifier()).isEqualTo(0);
        assertThat(adept.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cycling queues the boost before drawing and only boosts on resolution")
    void cyclingBoostUsesTheStack() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new FlamebladeAdept());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(adept.getPowerModifier()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(adept.getPowerModifier()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(adept.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent cycling does not boost your Adept")
    void opponentCyclingDoesNotBoostSelf() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new FlamebladeAdept());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new Censor()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        assertThat(adept.getPowerModifier()).isZero();
        assertThat(adept.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A noncycling discard boosts each Adept controlled by the discarding player")
    void ordinaryDiscardBoostsEachControlledAdept() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FlamebladeAdept());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FlamebladeAdept());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new FlamebladeAdept());
        harness.setHand(player1, List.of(new MiasmicMummy(), new Censor()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
        harness.assertInGraveyard(player1, "Censor");
    }

    @Test
    @DisplayName("Menace rejects a single blocker")
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new FlamebladeAdept());
        addCreatureReady(player2, new MiasmicMummy());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace permits two blockers")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new FlamebladeAdept());
        Permanent first = addCreatureReady(player2, new MiasmicMummy());
        Permanent second = addCreatureReady(player2, new MiasmicMummy());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
