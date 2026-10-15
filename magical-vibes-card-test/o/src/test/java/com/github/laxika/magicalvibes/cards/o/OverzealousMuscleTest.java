package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverzealousMuscle.class, Shock.class})
class OverzealousMuscleTest extends BaseCardTest {

    @Test
    @DisplayName("Gains indestructible when its controller commits a crime during their turn")
    void gainsIndestructibleAfterCrimeDuringOwnTurn() {
        Permanent muscle = addCreatureReady(player1, new OverzealousMuscle());

        castShockAt(player2.getId());

        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when its controller commits a crime during an opponent's turn")
    void doesNotTriggerDuringOpponentTurn() {
        Permanent muscle = addCreatureReady(player1, new OverzealousMuscle());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castShockAt(player2.getId());

        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible wears off at the end of the turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent muscle = addCreatureReady(player1, new OverzealousMuscle());
        castShockAt(player2.getId());
        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castShockAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Targeting yourself or your creature does not grant indestructible")
    void friendlyTargetsAreNotCrimes() {
        Permanent muscle = addCreatureReady(player1, new OverzealousMuscle());

        castShockAt(player1.getId());
        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isFalse();

        castShockAt(muscle.getId());
        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertOnBattlefield(player1, "Overzealous Muscle");
    }

    @Test
    @DisplayName("Targeting an opponent's creature grants indestructible only to your Muscle")
    void targetingOpposingCreatureIsCrime() {
        Permanent muscle = addCreatureReady(player1, new OverzealousMuscle());
        Permanent opposingMuscle = addCreatureReady(player2, new OverzealousMuscle());

        castShockAt(opposingMuscle.getId());

        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingMuscle, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The crime trigger resolves before the spell and protects against lethal damage")
    void triggerResolvesBeforeSpellAndPreventsLethalDamage() {
        Permanent muscle = addCreatureReady(player1, new OverzealousMuscle());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertLife(player2, 20);
        resolveAllTriggers();
        harness.assertLife(player2, 18);

        castShockAt(muscle.getId());
        castShockAt(muscle.getId());
        harness.assertOnBattlefield(player1, "Overzealous Muscle");
    }

    @Test
    @DisplayName("Each crime triggers, even after an earlier crime in the same turn")
    void triggersForEveryCrime() {
        Permanent muscle = addCreatureReady(player1, new OverzealousMuscle());
        castShockAt(player2.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, muscle, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A crime during an opponent's turn puts no Muscle ability on the stack")
    void opponentTurnCrimeDoesNotPutAbilityOnStack() {
        addCreatureReady(player1, new OverzealousMuscle());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }
}
