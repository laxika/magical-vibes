package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TormentedPariah.class})
class TormentedPariahTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Rampaging Werewolf when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent pariah = harness.addToBattlefieldAndReturn(player1, new TormentedPariah());

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(pariah.isTransformed()).isTrue();
        assertThat(pariah.getCard().getName()).isEqualTo("Rampaging Werewolf");
        assertThat(gqs.getEffectivePower(gd, pariah)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, pariah)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent pariah = harness.addToBattlefieldAndReturn(player1, new TormentedPariah());

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(pariah.isTransformed()).isFalse();
        assertThat(pariah.getCard().getName()).isEqualTo("Tormented Pariah");
    }

    @Test
    @DisplayName("Rampaging Werewolf transforms back when a player cast two or more spells last turn")
    void werewolfTransformsBackWhenTwoSpellsCast() {
        Permanent pariah = harness.addToBattlefieldAndReturn(player1, new TormentedPariah());

        // Transform to Rampaging Werewolf first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(pariah.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(pariah.isTransformed()).isFalse();
        assertThat(pariah.getCard().getName()).isEqualTo("Tormented Pariah");
        assertThat(gqs.getEffectivePower(gd, pariah)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pariah)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rampaging Werewolf does not transform back when only one spell was cast last turn")
    void werewolfDoesNotTransformWhenOneSpellCast() {
        Permanent pariah = harness.addToBattlefieldAndReturn(player1, new TormentedPariah());

        // Transform to Rampaging Werewolf first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(pariah.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(pariah.isTransformed()).isTrue();
        assertThat(pariah.getCard().getName()).isEqualTo("Rampaging Werewolf");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        Permanent pariah = harness.addToBattlefieldAndReturn(player1, new TormentedPariah());

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
        harness.passBothPriorities(); // resolve

        assertThat(pariah.isTransformed()).isTrue();
        assertThat(pariah.getCard().getName()).isEqualTo("Rampaging Werewolf");
    }

    @Test
    @DisplayName("An opponent's spell prevents the front face from transforming")
    void opponentSpellPreventsTransformation() {
        Permanent pariah = harness.addToBattlefieldAndReturn(player1, new TormentedPariah());
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(pariah.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Rampaging Werewolf stays transformed when no spells were cast")
    void werewolfStaysTransformedWhenNoSpellsCast() {
        Permanent pariah = harness.addToBattlefieldAndReturn(player1, new TormentedPariah());
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(pariah.isTransformed()).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(pariah.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The controller casting more than two spells transforms the back face on their upkeep")
    void controllerThreeSpellsTransformBackOnControllerUpkeep() {
        Permanent pariah = harness.addToBattlefieldAndReturn(player1, new TormentedPariah());
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(pariah.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player1.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(pariah.isTransformed()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(pariah.isTransformed()).isFalse();
    }
}
