package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.f.FolkMedicine;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlaringPain.class, FolkMedicine.class, MentalNote.class, Seedtime.class})
class SeedtimeTest extends BaseCardTest {

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

        harness.setHand(player1, List.of(new Seedtime()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
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
