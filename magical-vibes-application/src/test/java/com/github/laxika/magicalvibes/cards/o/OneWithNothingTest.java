package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.n.NeverendingTorment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OneWithNothing.class, ArabaMothrider.class, NeverendingTorment.class})
class OneWithNothingTest extends BaseCardTest {

    @Test
    @DisplayName("Discards the entire hand")
    void discardsEntireHand() {
        Card spell = new OneWithNothing();
        Card mothrider = new ArabaMothrider();
        Card torment = new NeverendingTorment();
        harness.setHand(player1, List.of(spell, mothrider, torment));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(spell, mothrider, torment);
    }

    @Test
    @DisplayName("Resolves when casting leaves the controller with an empty hand")
    void resolvesWithEmptyHand() {
        Card spell = new OneWithNothing();
        harness.setHand(player1, List.of(spell));
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Player two discards only their own hand on resolution")
    void playerTwoDiscardsOnlyTheirOwnHandOnResolution() {
        Card spell = new OneWithNothing();
        Card discarded = new ArabaMothrider();
        Card opponentCard = new NeverendingTorment();
        harness.setHand(player2, List.of(spell, discarded));
        harness.setHand(player1, List.of(opponentCard));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(spell, discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
