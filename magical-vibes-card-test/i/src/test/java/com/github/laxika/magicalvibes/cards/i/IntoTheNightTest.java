package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntoTheNight.class, DawnhartDisciple.class})
class IntoTheNightTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes night, discards two cards, then draws three")
    void becomesNightDiscardsTwoAndDrawsThree() {
        harness.setHand(player1, List.of(
                new IntoTheNight(), new DawnhartDisciple(), new DawnhartDisciple(), new DawnhartDisciple()));
        harness.setLibrary(player1, List.of(new DawnhartDisciple(), new DawnhartDisciple(), new DawnhartDisciple()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();

        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Becomes night and draws one when zero cards are discarded")
    void becomesNightAndDrawsOneWhenDiscardingZero() {
        harness.setHand(player1, List.of(new IntoTheNight(), new DawnhartDisciple()));
        harness.setLibrary(player1, List.of(new DawnhartDisciple()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Becomes night and draws one with an empty hand")
    void becomesNightAndDrawsOneWithEmptyHand() {
        harness.setHand(player1, List.of(new IntoTheNight()));
        harness.setLibrary(player1, List.of(new DawnhartDisciple()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Changes day to night before the discard choice")
    void changesDayToNightBeforeDiscardChoice() {
        gd.dayNight = DayNight.DAY;
        harness.setHand(player1, List.of(new IntoTheNight(), new DawnhartDisciple()));
        harness.setLibrary(player1, List.of(new DawnhartDisciple()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.handleXValueChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Still discards and draws when it is already night")
    void discardsEntireHandAndDrawsWhenAlreadyNight() {
        gd.dayNight = DayNight.NIGHT;
        DawnhartDisciple firstDiscard = new DawnhartDisciple();
        DawnhartDisciple secondDiscard = new DawnhartDisciple();
        DawnhartDisciple firstDraw = new DawnhartDisciple();
        DawnhartDisciple secondDraw = new DawnhartDisciple();
        DawnhartDisciple thirdDraw = new DawnhartDisciple();
        harness.setHand(player1, List.of(new IntoTheNight(), firstDiscard, secondDiscard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.setHand(player2, List.of(new DawnhartDisciple()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDiscard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstDiscard, secondDiscard).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
