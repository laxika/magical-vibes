package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarksteelMonolith.class, GrizzlyBears.class, MindStone.class})
class DarksteelMonolithTest extends BaseCardTest {

    private Card colorlessInstant() {
        Card card = new Card();
        card.setName("Colorless Instant");
        card.setType(CardType.INSTANT);
        card.setManaCost("{2}");
        return card;
    }

    @Test
    @DisplayName("Casts a colorless spell from hand for free")
    void castsColorlessSpellFromHandForFree() {
        harness.addToBattlefield(player1, new DarksteelMonolith());
        harness.setHand(player1, List.of(new MindStone()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce the cost of a colored spell")
    void doesNotReduceColoredSpellCost() {
        harness.addToBattlefield(player1, new DarksteelMonolith());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be used only once each turn")
    void canBeUsedOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new DarksteelMonolith());
        harness.setHand(player1, List.of(new MindStone(), new MindStone()));

        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be used during an opponent's turn")
    void canBeUsedDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new DarksteelMonolith());
        harness.setHand(player1, List.of(colorlessInstant()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
