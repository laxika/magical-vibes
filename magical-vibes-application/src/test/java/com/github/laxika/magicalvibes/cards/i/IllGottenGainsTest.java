package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IllGottenGainsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player discards their hand and returns up to three graveyard cards")
    void eachPlayerDiscardsAndReturnsUpToThreeCards() {
        List<Card> player1Hand = List.of(
                new IllGottenGains(), new GrizzlyBears(), new LightningBolt(),
                new GrizzlyBears(), new LightningBolt());
        List<Card> player2Hand = List.of(
                new GrizzlyBears(), new LightningBolt(), new GrizzlyBears(), new LightningBolt());
        harness.setHand(player1, player1Hand);
        harness.setHand(player2, player2Hand);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).playerId())
                .isEqualTo(player1.getId());

        chooseThree(player1);
        chooseThree(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ill-Gotten Gains"));
    }

    private void chooseThree(Player player) {
        harness.handleGraveyardCardChosen(player, 0);
        harness.handleGraveyardCardChosen(player, 0);
        harness.handleGraveyardCardChosen(player, 0);
    }
}
