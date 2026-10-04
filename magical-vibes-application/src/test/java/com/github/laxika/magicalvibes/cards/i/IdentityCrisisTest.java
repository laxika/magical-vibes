package com.github.laxika.magicalvibes.cards.i;

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

@CardUsed({IdentityCrisis.class, GrizzlyBears.class, Peek.class, Shock.class, Island.class})
class IdentityCrisisTest extends BaseCardTest {

    private void addCost() {
        // {2}{W}{W}{B}{B} — surplus is harmless and keeps payment order-independent.
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 4);
    }

    @Test
    @DisplayName("Exiles all cards from target player's hand and graveyard")
    void exilesHandAndGraveyard() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock()));
        harness.setGraveyard(player2, List.of(new Peek(), new Island()));
        harness.setHand(player1, List.of(new IdentityCrisis()));
        addCost();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Only the targeted player's zones are affected")
    void onlyAffectsTargetPlayer() {
        harness.setHand(player1, List.of(new IdentityCrisis(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Peek()));
        harness.setGraveyard(player2, List.of(new Island()));
        addCost();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Target player's hand and graveyard exiled.
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);

        // Caster untouched: still holds Grizzly Bears, graveyard keeps Shock, nothing exiled.
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolves with no error when the target has an empty hand and graveyard")
    void emptyZonesNoError() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new IdentityCrisis()));
        addCost();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target its controller without exiling the resolving spell")
    void canTargetController() {
        IdentityCrisis spell = new IdentityCrisis();
        IdentityCrisis handCard = new IdentityCrisis();
        IdentityCrisis graveyardCard = new IdentityCrisis();
        harness.setHand(player1, List.of(spell, handCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        addCost();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(handCard, graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Exiles the graveyard even when the target's hand is empty")
    void emptyHandDoesNotPreventGraveyardExile() {
        IdentityCrisis graveyardCard = new IdentityCrisis();
        harness.setHand(player1, List.of(new IdentityCrisis()));
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(graveyardCard));
        addCost();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
    }

    @Test
    @DisplayName("Exiles the hand even when the target's graveyard is empty")
    void emptyGraveyardDoesNotPreventHandExile() {
        IdentityCrisis handCard = new IdentityCrisis();
        harness.setHand(player1, List.of(new IdentityCrisis()));
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of());
        addCost();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(handCard);
    }
}
