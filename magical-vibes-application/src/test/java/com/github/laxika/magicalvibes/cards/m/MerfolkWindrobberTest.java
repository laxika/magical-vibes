package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerfolkWindrobber.class, GrizzlyBears.class, AirElemental.class})
class MerfolkWindrobberTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player mills one card from that player's library")
    void combatDamageMillsOneCard() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        addCreatureReady(player1, new MerfolkWindrobber());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
    }

    @Test
    @DisplayName("A blocked Merfolk Windrobber does not mill a card")
    void blockedCombatDoesNotMill() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        addCreatureReady(player1, new MerfolkWindrobber());
        addCreatureReady(player2, new AirElemental());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Sacrificing Merfolk Windrobber draws a card when an opponent has eight graveyard cards")
    void sacrificeDrawsWithEightOpponentGraveyardCards() {
        harness.setGraveyard(player2, filler(8));
        Permanent windrobber = addCreatureReady(player1, new MerfolkWindrobber());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(windrobber);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(windrobber.getCard());
    }

    @Test
    @DisplayName("The sacrifice ability cannot be activated without enough cards in an opponent's graveyard")
    void sacrificeCannotActivateBelowThreshold() {
        harness.setGraveyard(player2, filler(7));
        addCreatureReady(player1, new MerfolkWindrobber());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The controller's graveyard does not satisfy the sacrifice ability condition")
    void ownGraveyardDoesNotSatisfyCondition() {
        harness.setGraveyard(player1, filler(8));
        addCreatureReady(player1, new MerfolkWindrobber());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and the graveyard threshold is not checked again on resolution")
    void drawResolvesAfterOpponentGraveyardShrinks() {
        harness.setGraveyard(player2, filler(8));
        Card drawnCard = new MerfolkWindrobber();
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent windrobber = harness.addToBattlefieldAndReturn(player1, new MerfolkWindrobber());
        windrobber.setTapped(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(windrobber);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(windrobber.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Milling the eighth graveyard card enables the sacrifice ability")
    void combatMillEnablesSacrifice() {
        harness.setGraveyard(player2, filler(7));
        Card milledCard = new MerfolkWindrobber();
        Card drawnCard = new MerfolkWindrobber();
        harness.setLibrary(player2, List.of(milledCard));
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent windrobber = addCreatureReady(player1, new MerfolkWindrobber());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(milledCard).hasSize(8);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(windrobber);
    }

    @Test
    @DisplayName("The damaged player mills only the top card when the opponent controls Windrobber")
    void opponentControlledWindrobberMillsDamagedPlayer() {
        Card topCard = new MerfolkWindrobber();
        Card secondCard = new MerfolkWindrobber();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        addCreatureReady(player2, new MerfolkWindrobber());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard).doesNotContain(secondCard);
    }

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
