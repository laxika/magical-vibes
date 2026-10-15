package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallousBloodmage.class, Forest.class, Shock.class})
class CallousBloodmageTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Pest token whose death gains 1 life")
    void createsPestWithDeathTrigger() {
        castBloodmage(0);
        resolveAllTriggers();

        Permanent pest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Pest"))
                .findFirst()
                .orElseThrow();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, pest.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Draws a card and loses 1 life")
    void drawsAndLosesLife() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new CallousBloodmage()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0, 1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Exiles the targeted player's graveyard")
    void exilesTargetPlayersGraveyard() {
        harness.setGraveyard(player2, List.of(new Forest(), new Shock()));

        castBloodmage(2);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can exile its controller's graveyard without affecting the opponent's")
    void exilesOwnGraveyard() {
        CallousBloodmage ownCard = new CallousBloodmage();
        CallousBloodmage opponentCard = new CallousBloodmage();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        castBloodmage(2);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Pest");
    }

    @Test
    @DisplayName("Can target an empty graveyard without selecting another mode")
    void exilesEmptyGraveyard() {
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player1, List.of(new CallousBloodmage()));

        castBloodmage(2);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Pest");
    }

    private void castBloodmage(int mode) {
        harness.setHand(player1, List.of(new CallousBloodmage()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0, mode);
    }

}
