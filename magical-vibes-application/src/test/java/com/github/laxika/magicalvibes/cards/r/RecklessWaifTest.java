package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.v.VampiricFury;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessWaif.class, VampiricFury.class})
class RecklessWaifTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Merciless Predator when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent waif = harness.addToBattlefieldAndReturn(player1, new RecklessWaif());

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(waif.isTransformed()).isTrue();
        assertThat(waif.getCard().getName()).isEqualTo("Merciless Predator");
        assertThat(gqs.getEffectivePower(gd, waif)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, waif)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent waif = harness.addToBattlefieldAndReturn(player1, new RecklessWaif());

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(waif.isTransformed()).isFalse();
        assertThat(waif.getCard().getName()).isEqualTo("Reckless Waif");
    }

    @Test
    @DisplayName("Merciless Predator transforms back when a player cast two or more spells last turn")
    void predatorTransformsBackWhenTwoSpellsCast() {
        Permanent waif = harness.addToBattlefieldAndReturn(player1, new RecklessWaif());

        // Transform to Merciless Predator first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(waif.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(waif.isTransformed()).isFalse();
        assertThat(waif.getCard().getName()).isEqualTo("Reckless Waif");
        assertThat(gqs.getEffectivePower(gd, waif)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, waif)).isEqualTo(1);
    }

    @Test
    @DisplayName("Merciless Predator does not transform back when only one spell was cast last turn")
    void predatorDoesNotTransformWhenOneSpellCast() {
        Permanent waif = harness.addToBattlefieldAndReturn(player1, new RecklessWaif());

        // Transform to Merciless Predator first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(waif.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(waif.isTransformed()).isTrue();
        assertThat(waif.getCard().getName()).isEqualTo("Merciless Predator");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        Permanent waif = harness.addToBattlefieldAndReturn(player1, new RecklessWaif());

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
        harness.passBothPriorities(); // resolve

        assertThat(waif.isTransformed()).isTrue();
        assertThat(waif.getCard().getName()).isEqualTo("Merciless Predator");
    }

    @Test
    @DisplayName("A spell cast in response this upkeep does not prevent transforming")
    void currentTurnSpellDoesNotPreventTransform() {
        Permanent waif = harness.addToBattlefieldAndReturn(player1, new RecklessWaif());
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(waif.isTransformed()).isFalse();
        harness.castFromHand(player2, new VampiricFury(), "{1}{R}");
        harness.passBothPriorities();
        assertThat(waif.isTransformed()).isFalse();
        harness.passBothPriorities();

        assertThat(waif.isTransformed()).isTrue();
        assertThat(waif.getCard().getName()).isEqualTo("Merciless Predator");
    }

    @Test
    @DisplayName("An opponent's spell last turn prevents the front-face trigger")
    void opponentSpellLastTurnPreventsTransform() {
        Permanent waif = harness.addToBattlefieldAndReturn(player1, new RecklessWaif());
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(waif.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Merciless Predator stays transformed on a subsequent spell-free upkeep")
    void predatorDoesNotTransformWhenNoSpellsCast() {
        Permanent waif = harness.addToBattlefieldAndReturn(player1, new RecklessWaif());
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(waif.isTransformed()).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(waif.isTransformed()).isTrue();
        assertThat(waif.getCard().getName()).isEqualTo("Merciless Predator");
    }

}
