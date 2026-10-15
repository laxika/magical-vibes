package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.f.FolkMedicine;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlaringPain.class, FolkMedicine.class, MentalNote.class, PaintersServant.class, Seedtime.class})
class SeedtimeTest extends BaseCardTest {

    @Test
    void opponentBlueSpellStillOnStackSatisfiesTheCondition() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new MentalNote(), "{U}");
        harness.castFromHand(player1, new Seedtime(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void opponentBlueSpellCastInResponseSatisfiesTheConditionAtResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Seedtime(), "{1}{G}");
        harness.castFromHand(player2, new MentalNote(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void extraTurnBeginsImmediatelyAfterCurrentTurnAndDoesNotReuseSpellHistory() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.castFromHand(player2, new MentalNote(), "{U}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new Seedtime(), "{1}{G}");
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentTurnIsExtraTurn).isTrue();
        assertThat(gd.extraTurns).isEmpty();
        harness.castFromHand(player1, new Seedtime(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @CardUsed({PaintersServant.class, FolkMedicine.class, Seedtime.class})
    void opponentSpellMadeBlueByPaintersServantSatisfiesTheCondition() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new PaintersServant(), "{2}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.castFromHand(player2, new FolkMedicine(), "{2}{G}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new Seedtime(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void takesAnExtraTurnAfterOpponentCastsBlueSpellDuringYourTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
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
    void doesNotTakeAnExtraTurnAfterOpponentCastsNonBlueSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new FolkMedicine(), "{2}{G}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new Seedtime(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void yourOwnBlueSpellDoesNotSatisfyTheCondition() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MentalNote(), "{U}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new Seedtime(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void blueSpellFromPreviousTurnDoesNotSatisfyTheCondition() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new MentalNote(), "{U}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

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
}
