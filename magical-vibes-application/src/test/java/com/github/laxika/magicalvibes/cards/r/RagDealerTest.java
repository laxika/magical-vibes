package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RagDealer.class, HumbleBudoka.class, BlessedBreath.class, Forest.class})
class RagDealerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles three chosen cards from an opponent's graveyard")
    void exilesThreeCardsFromOpponentGraveyard() {
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        Card card1 = new HumbleBudoka();
        Card card2 = new BlessedBreath();
        Card card3 = new Forest();
        harness.setGraveyard(player2, List.of(card1, card2, card3));
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
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        Card card1 = new HumbleBudoka();
        Card card2 = new BlessedBreath();
        harness.setGraveyard(player1, List.of(card1, card2));
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
    @DisplayName("Can activate without choosing any graveyard cards")
    void canActivateWithoutChoosingTargets() {
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        Card card = new HumbleBudoka();
        harness.setGraveyard(player2, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).containsExactly(card.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(dealer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Targets must all come from a single graveyard")
    void targetsMustShareOneGraveyard() {
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        Card mine = new HumbleBudoka();
        Card theirs = new BlessedBreath();
        harness.setGraveyard(player1, List.of(mine));
        harness.setGraveyard(player2, List.of(theirs));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0,
                List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    @DisplayName("Cannot target more than three cards")
    void cannotTargetMoreThanThree() {
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        Card card1 = new HumbleBudoka();
        Card card2 = new BlessedBreath();
        Card card3 = new Forest();
        Card card4 = new HumbleBudoka();
        harness.setGraveyard(player2, List.of(card1, card2, card3, card4));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0,
                List.of(card1.getId(), card2.getId(), card3.getId(), card4.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than 3");
    }

    @Test
    @DisplayName("Activating taps Rag Dealer")
    void activatingTapsDealer() {
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        Card card1 = new HumbleBudoka();
        harness.setGraveyard(player2, List.of(card1));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0, List.of(card1.getId()));

        assertThat(dealer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        Card card1 = new HumbleBudoka();
        harness.setGraveyard(player2, List.of(card1));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0,
                List.of(card1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that leaves the graveyard before resolution is skipped")
    void removedTargetIsSkipped() {
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        Card card1 = new HumbleBudoka();
        Card card2 = new BlessedBreath();
        harness.setGraveyard(player2, List.of(card1, card2));
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

    @Test
    void cannotChooseTheSameCardTwice() {
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        Card card = new HumbleBudoka();
        harness.setGraveyard(player2, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0,
                List.of(card.getId(), card.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("same card twice");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        dealer.tap();
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0,
                List.of())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        dealer.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0,
                List.of())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void genericManaCannotPayTheBlackRequirement() {
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0,
                List.of())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityStillResolvesAfterDealerLeavesBattlefield() {
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        Card card = new HumbleBudoka();
        harness.setGraveyard(player2, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0, List.of(card.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(dealer);
        harness.setGraveyard(player1, List.of(dealer.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).containsExactly(card.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).containsExactly(dealer.getCard().getId());
    }

    @Test
    void noCardsAreExiledWhenAllTargetsLeaveTheGraveyard() {
        Permanent dealer = addCreatureReady(player1, new RagDealer());
        Card target = new HumbleBudoka();
        Card untargeted = new BlessedBreath();
        harness.setGraveyard(player2, List.of(target, untargeted));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbilityWithGraveyardTargets(player1, dealerIndex(dealer), 0, List.of(target.getId()));
        harness.setGraveyard(player2, List.of(untargeted));
        harness.setHand(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).containsExactly(untargeted.getId());
        assertThat(dealer.isTapped()).isTrue();
    }

}
