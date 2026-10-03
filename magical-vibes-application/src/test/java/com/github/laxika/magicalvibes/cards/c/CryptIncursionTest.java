package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CryptIncursion.class, GrizzlyBears.class, Peek.class, Shock.class})
class CryptIncursionTest extends BaseCardTest {

    private void addCost() {
        harness.addMana(player1, ManaColor.BLACK, 3);
    }

    @Test
    @DisplayName("Exiles only creature cards from the target's graveyard and gains 3 life per card")
    void exilesCreaturesAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Shock(), new Peek()));
        harness.setHand(player1, List.of(new CryptIncursion()));
        addCost();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("No creature cards in the target's graveyard gains no life")
    void noCreaturesNoLife() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player2, List.of(new Peek()));
        harness.setHand(player1, List.of(new CryptIncursion()));
        addCost();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CryptIncursion()));
        addCost();

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("An empty graveyard is a legal target and gains no life")
    void emptyGraveyard() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new CryptIncursion()));
        addCost();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Crypt Incursion");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Counts creature cards in the target graveyard at resolution, leaving other graveyards alone")
    void usesGraveyardAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CryptIncursion()));
        addCost();

        harness.castInstant(player1, 0, player2.getId());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new Peek()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Peek");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 26);
        harness.assertLife(player2, 20);
    }
}
