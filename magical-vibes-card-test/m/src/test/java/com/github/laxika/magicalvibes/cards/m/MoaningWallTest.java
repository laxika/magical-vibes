package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoaningWall.class, GrizzlyBears.class})
class MoaningWallTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new MoaningWall()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Moaning Wall");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Defender prevents attacking even when ready")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new MoaningWall());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Cycling accepts colorless mana and discards before drawing on resolution")
    void cyclingPaysCostsBeforeResolution() {
        MoaningWall source = new MoaningWall();
        MoaningWall drawnCard = new MoaningWall();
        MoaningWall secondCard = new MoaningWall();
        harness.setHand(player1, List.of(source));
        harness.setLibrary(player1, List.of(drawnCard, secondCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard, secondCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
    }

    @Test
    @DisplayName("Cycling requires two mana and leaves the card in hand when payment fails")
    void cyclingRequiresTwoMana() {
        MoaningWall source = new MoaningWall();
        harness.setHand(player1, List.of(source));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        harness.assertNotInGraveyard(player1, "Moaning Wall");
        assertThat(gd.stack).isEmpty();
    }
}
