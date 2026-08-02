package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RagDealerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles three chosen cards from an opponent's graveyard")
    void exilesThreeCardsFromOpponentGraveyard() {
        Permanent dealer = addReadyDealer(player1);
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        Card card3 = new Forest();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2, card3)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0,
                List.of(card1.getId(), card2.getId(), card3.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Can exile fewer than three cards, and from the controller's own graveyard")
    void exilesFewerCardsFromOwnGraveyard() {
        Permanent dealer = addReadyDealer(player1);
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(card1, card2)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0,
                List.of(card1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).containsExactly(card2.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactly(card1.getId());
    }

    @Test
    @DisplayName("Targets must all come from a single graveyard")
    void targetsMustShareOneGraveyard() {
        Permanent dealer = addReadyDealer(player1);
        Card mine = new GrizzlyBears();
        Card theirs = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(mine)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(theirs)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0,
                List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    @DisplayName("Cannot target more than three cards")
    void cannotTargetMoreThanThree() {
        Permanent dealer = addReadyDealer(player1);
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        Card card3 = new Forest();
        Card card4 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2, card3, card4)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0,
                List.of(card1.getId(), card2.getId(), card3.getId(), card4.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than 3");
    }

    @Test
    @DisplayName("Activating taps Rag Dealer")
    void activatingTapsDealer() {
        Permanent dealer = addReadyDealer(player1);
        Card card1 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0, List.of(card1.getId()));

        assertThat(dealer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent dealer = addReadyDealer(player1);
        Card card1 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0,
                List.of(card1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that leaves the graveyard before resolution is skipped")
    void removedTargetIsSkipped() {
        Permanent dealer = addReadyDealer(player1);
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0,
                List.of(card1.getId(), card2.getId()));

        gd.playerGraveyards.get(player2.getId()).removeIf(c -> c.getId().equals(card1.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).containsExactly(card2.getId());
    }

    private int dealerIndex(Permanent dealer) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(dealer);
    }

    private Permanent addReadyDealer(Player player) {
        Permanent perm = new Permanent(new RagDealer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
