package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurgeTheProfaneTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent discards two cards and the caster gains 2 life")
    void opponentDiscardsTwoAndCasterGainsLife() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        harness.setLife(player1, 20);
        castPurgeTheProfane();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Purge the Profane");
    }

    @Test
    @DisplayName("Caster still gains 2 life when the opponent's hand is empty")
    void gainsLifeWithEmptyOpponentHand() {
        harness.setHand(player2, new ArrayList<>());
        harness.setLife(player1, 20);
        castPurgeTheProfane();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new PurgeTheProfane()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castPurgeTheProfane() {
        harness.setHand(player1, List.of(new PurgeTheProfane()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player2.getId());
    }
}
