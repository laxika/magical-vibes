package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TraumaticRevelation.class, GrizzlyBears.class, Peek.class})
class TraumaticRevelationTest extends BaseCardTest {

    @Test
    void choosesAndDiscardsCreatureFromOpponentHand() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new Peek()));
        cast();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).singleElement()
                .extracting(card -> card.getName()).isEqualTo("Peek");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void decliningChoiceIncubatesThree() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new Peek()));
        cast();

        harness.handleCardChosen(player1, -1);

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void noMatchingCardIncubatesWithoutPrompt() {
        harness.setHand(player2, List.of(new Peek()));
        cast();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Peek");
    }

    @Test
    void canOnlyTargetOpponent() {
        harness.setHand(player1, List.of(new TraumaticRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void cast() {
        harness.setHand(player1, List.of(new TraumaticRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
