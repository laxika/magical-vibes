package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElixirOfImmortality;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicAccord.class, ElixirOfImmortality.class})
class AngelicAccordTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Creates a 4/4 white flying Angel token when you gained 4 life this turn")
    void createsAngelTokenOnFourLifeGained() {
        harness.addToBattlefield(player1, new AngelicAccord());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        advanceToEndStep(player1);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        var angels = findPermanents(player1, "Angel");
        assertThat(angels).hasSize(1);
        assertThat(angels).allSatisfy(t -> {
            assertThat(t.getCard().getPower()).isEqualTo(4);
            assertThat(t.getCard().getToughness()).isEqualTo(4);
            assertThat(t.getCard().isToken()).isTrue();
            assertThat(gqs.hasKeyword(gd, t, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Creates a token when you gained more than 4 life this turn")
    void createsAngelTokenOnMoreThanFourLifeGained() {
        harness.addToBattlefield(player1, new AngelicAccord());
        gd.lifeGainedThisTurn.put(player1.getId(), 7);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates no token when you gained fewer than 4 life this turn")
    void noTokenBelowThreshold() {
        harness.addToBattlefield(player1, new AngelicAccord());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creates no token when you gained no life this turn")
    void noTokenWithoutLifeGain() {
        harness.addToBattlefield(player1, new AngelicAccord());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers on each end step, including the opponent's")
    void triggersOnOpponentEndStep() {
        harness.addToBattlefield(player1, new AngelicAccord());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);
    }

    @Test
    @DisplayName("Life gained by the opponent does not trigger your Angelic Accord")
    void opponentLifeGainDoesNotTrigger() {
        harness.addToBattlefield(player1, new AngelicAccord());
        gd.lifeGainedThisTurn.put(player2.getId(), 8);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isZero();
    }

    @Test
    @DisplayName("Life gained before Accord enters still counts")
    void lifeGainedBeforeEnteringCounts() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addToBattlefield(player1, new ElixirOfImmortality());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 25);
        harness.addToBattlefield(player1, new AngelicAccord());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);
    }

    @Test
    @DisplayName("Gaining life after the end step begins cannot trigger Accord retroactively")
    void lifeGainedDuringEndStepIsTooLate() {
        harness.addToBattlefield(player1, new AngelicAccord());
        harness.addToBattlefield(player1, new ElixirOfImmortality());
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Angel")).isZero();
    }

    @Test
    @DisplayName("Each Accord creates only one Angel regardless of excess life gain")
    void multipleAccordsEachTriggerOnce() {
        harness.addToBattlefield(player1, new AngelicAccord());
        harness.addToBattlefield(player1, new AngelicAccord());
        gd.lifeGainedThisTurn.put(player1.getId(), 12);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isEqualTo(2);
    }

    @Test
    @DisplayName("Losing life after qualifying does not undo the trigger")
    void lowerLifeTotalDoesNotUndoLifeGained() {
        harness.addToBattlefield(player1, new AngelicAccord());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player1, 10);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);
    }
}
