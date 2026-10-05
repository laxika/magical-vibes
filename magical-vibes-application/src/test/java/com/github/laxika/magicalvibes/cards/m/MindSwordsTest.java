package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindSwords.class, GrizzlyBears.class, Forest.class, Swamp.class})
class MindSwordsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player exiles two cards from their hand")
    void eachPlayerExilesTwoCards() {
        harness.setHand(player1, List.of(new MindSwords(), new GrizzlyBears(), new Forest()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Players with fewer than two cards exile their entire hands")
    void exilesEntireShortHands() {
        harness.setHand(player1, List.of(new MindSwords(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleCardChosen(player1, 0);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can be cast for its alternate cost by sacrificing a creature while controlling a Swamp")
    void castsForAlternateCost() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindSwords(), new GrizzlyBears(), new Forest()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));

        harness.castWithAlternateCost(player1, 0, List.of(paymentCreature.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Alternate cost requires control of a Swamp")
    void alternateCostRequiresSwamp() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindSwords()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(paymentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Alternate cost requires a creature to sacrifice")
    void alternateCostRequiresCreature() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new MindSwords()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exile waits until every player has chosen, preserving hidden choices")
    void exilesAfterAllPlayersChoose() {
        harness.setHand(player1, List.of(new MindSwords(), new GrizzlyBears(), new Forest(), new Swamp()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        harness.assertInHand(player1, "Swamp");
        harness.assertInHand(player2, "Swamp");
    }

    @Test
    @DisplayName("Skips an empty caster hand while the opponent exiles their short hand")
    void skipsEmptyCasterHand() {
        harness.setHand(player1, List.of(new MindSwords()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Mind Swords");
    }

    @Test
    @DisplayName("Resolves when both hands are empty")
    void resolvesWithEmptyHands() {
        harness.setHand(player1, List.of(new MindSwords()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mind Swords");
    }

    @Test
    @DisplayName("Alternate cost rejects sacrificing a noncreature")
    void alternateCostRejectsNoncreature() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player1, List.of(new MindSwords()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(swamp.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required filter");
        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertInHand(player1, "Mind Swords");
    }
}
