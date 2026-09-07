package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuthlessNegotiation.class, Forest.class, GrizzlyBears.class})
class RuthlessNegotiationTest extends BaseCardTest {

    @Test
    void exilesAChosenCardFromTargetOpponentsHand() {
        Card chosen = new GrizzlyBears();
        Card remaining = new Forest();
        harness.setHand(player2, List.of(chosen, remaining));
        harness.setHand(player1, List.of(new RuthlessNegotiation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void flashbackExilesAChosenCardAndDrawsACard() {
        Card chosen = new GrizzlyBears();
        Card remaining = new Forest();
        Card draw = new Forest();
        harness.setHand(player2, List.of(chosen, remaining));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new RuthlessNegotiation()));
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ruthless Negotiation"));
    }

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new RuthlessNegotiation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
