package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RemorselessPunishment.class, Forest.class, GrizzlyBears.class, LilianaVess.class})
class RemorselessPunishmentTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent loses 10 life when neither alternative is available")
    void losesTenLifeWithoutAlternatives() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Forest()));

        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Target opponent discards two cards for one iteration and loses life for the other")
    void discardsTwoCardsForOneIteration() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(first, second));

        cast();

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.discard(2));
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Target opponent can sacrifice a creature or planeswalker, but not a land")
    void sacrificesCreatureOrPlaneswalker() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.sacrifice(
                "a creature or planeswalker"));
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Target opponent can sacrifice a planeswalker")
    void sacrificesPlaneswalker() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefieldAndReturn(player2, new LilianaVess())
                .setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 5);

        cast();

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.sacrifice(
                "a creature or planeswalker"));
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Liliana Vess"));

        harness.assertNotOnBattlefield(player2, "Liliana Vess");
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new RemorselessPunishment()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void cast() {
        harness.setHand(player1, List.of(new RemorselessPunishment()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
