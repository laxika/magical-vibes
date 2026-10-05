package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LurchingRotbeast.class})
class LurchingRotbeastTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new LurchingRotbeast()));
        LurchingRotbeast drawnCard = new LurchingRotbeast();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lurching Rotbeast");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Cycling discards as a cost before drawing on resolution")
    void cyclingDiscardsBeforeResolution() {
        LurchingRotbeast cycledCard = new LurchingRotbeast();
        LurchingRotbeast drawnCard = new LurchingRotbeast();
        LurchingRotbeast remainingCard = new LurchingRotbeast();
        harness.setHand(player1, List.of(cycledCard));
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycledCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard, remainingCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycledCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated without mana")
    void cyclingRequiresMana() {
        LurchingRotbeast card = new LurchingRotbeast();
        harness.setHand(player1, List.of(card));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        harness.assertNotInGraveyard(player1, "Lurching Rotbeast");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling requires black mana rather than another color")
    void cyclingRequiresBlackMana() {
        LurchingRotbeast card = new LurchingRotbeast();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        harness.assertNotInGraveyard(player1, "Lurching Rotbeast");
        assertThat(gd.stack).isEmpty();
    }
}
