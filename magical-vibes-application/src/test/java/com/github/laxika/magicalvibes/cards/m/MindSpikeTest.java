package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindSpike.class, Forest.class, GrizzlyBears.class, Shock.class})
class MindSpikeTest extends BaseCardTest {

    @Test
    void choosesAndDiscardsNoncreatureNonlandCardThenLosesLife() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        cast(List.of(forest, bears, shock));

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(2);

        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest, bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(shock);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void drawsWhenTargetHasNoNoncreatureNonlandCards() {
        Card drawn = new Shock();
        harness.setLibrary(player1, List.of(drawn));
        cast(List.of(new Forest(), new GrizzlyBears()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canOnlyTargetAnOpponent() {
        harness.setHand(player1, List.of(new MindSpike()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void cast(List<Card> targetHand) {
        harness.setHand(player1, List.of(new MindSpike()));
        harness.setHand(player2, targetHand);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
