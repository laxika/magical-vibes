package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WakeTheDead.class, GrizzlyBears.class, HolyDay.class})
class WakeTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Returns exactly X target creatures and sacrifices them at the next end step")
    void returnsExactlyXCreaturesAndSacrificesThemAtNextEndStep() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new WakeTheDead()));
        addManaForX(2);
        putPlayer1InOpponentsCombat();

        harness.castInstantForX(player1, 0, 2, List.of());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Wake the Dead");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Wake the Dead", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Requires exactly X matching creature cards in the graveyard")
    void rejectsTooFewCreatureCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HolyDay()));
        harness.setHand(player1, List.of(new WakeTheDead()));
        addManaForX(2);
        putPlayer1InOpponentsCombat();

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough matching creature cards in graveyard");
    }

    @Test
    @DisplayName("Can only be cast during an opponent's combat")
    void cannotBeCastOutsideOpponentsCombat() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WakeTheDead()));
        addManaForX(1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNonCreatureCard() {
        Card noncreature = new HolyDay();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new WakeTheDead()));
        addManaForX(1);
        putPlayer1InOpponentsCombat();

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForX(int x) {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, x);
    }

    private void putPlayer1InOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
    }
}
