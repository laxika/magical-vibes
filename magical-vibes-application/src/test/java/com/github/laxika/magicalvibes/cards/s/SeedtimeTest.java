package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Seedtime.class, MentalNote.class, FlaringPain.class})
class SeedtimeTest extends BaseCardTest {

    @Test
    void takesAnExtraTurnAfterOpponentCastsBlueSpellDuringYourTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passPriority(player1);
        harness.castFromHand(player2, new MentalNote(), "{U}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new Seedtime(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void doesNotTakeAnExtraTurnWithoutOpponentBlueSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Seedtime(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void doesNotTakeAnExtraTurnWhenOpponentCastsNonBlueSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passPriority(player1);
        harness.castFromHand(player2, new FlaringPain(), "{1}{R}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new Seedtime(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void doesNotTakeAnExtraTurnWhenControllerCastsBlueSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MentalNote(), "{U}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new Seedtime(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void cannotBeCastDuringOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new MentalNote(), "{U}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromHand(player1, new Seedtime(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
