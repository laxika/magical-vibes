package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Seedtime.class, MentalNote.class})
class SeedtimeTest extends BaseCardTest {

    @Test
    void takesAnExtraTurnAfterOpponentCastsBlueSpellDuringYourTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new MentalNote()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Seedtime()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void doesNotTakeAnExtraTurnWithoutOpponentBlueSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Seedtime()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void cannotBeCastDuringOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new MentalNote()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0);

        harness.setHand(player1, List.of(new Seedtime()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
