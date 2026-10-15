package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChandraSparkHunter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BroadsideBarrage.class, BurnoutBashtronaut.class, Forest.class, ChandraSparkHunter.class})
class BroadsideBarrageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage, then draws and discards a card")
    void dealsDamageThenDrawsAndDiscards() {
        harness.addToBattlefield(player2, new BurnoutBashtronaut());
        harness.setHand(player1, List.of(new BroadsideBarrage(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        addBroadsideBarrageMana();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Burnout Bashtronaut"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player2, "Burnout Bashtronaut");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new BroadsideBarrage()));
        addBroadsideBarrageMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals exactly 5 damage to a planeswalker and loots only for the caster")
    void damagesPlaneswalkerAndDiscardsNewlyDrawnCard() {
        var chandra = harness.addToBattlefieldAndReturn(player2, new ChandraSparkHunter());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        var originalHandCard = new Forest();
        var drawnCard = new Forest();
        var opponentHandCard = new Forest();
        harness.setHand(player1, List.of(new BroadsideBarrage(), originalHandCard));
        harness.setHand(player2, List.of(opponentHandCard));
        harness.setLibrary(player1, List.of(drawnCard));
        addBroadsideBarrageMana();

        harness.castInstant(player1, 0, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(originalHandCard, drawnCard);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        harness.assertOnBattlefield(player2, "Chandra, Spark Hunter");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(originalHandCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentHandCard);
    }

    @Test
    @DisplayName("Does not draw or discard when its only target leaves before resolution")
    void illegalTargetPreventsLooting() {
        var target = harness.addToBattlefieldAndReturn(player2, new BurnoutBashtronaut());
        var handCard = new Forest();
        var libraryCard = new Forest();
        harness.setHand(player1, List.of(new BroadsideBarrage(), handCard));
        harness.setLibrary(player1, List.of(libraryCard));
        addBroadsideBarrageMana();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Broadside Barrage");
    }

    @Test
    @DisplayName("Draws and discards even when cast from an otherwise empty hand")
    void discardsOnlyDrawnCard() {
        harness.addToBattlefield(player1, new BurnoutBashtronaut());
        var drawnCard = new Forest();
        harness.setHand(player1, List.of(new BroadsideBarrage()));
        harness.setLibrary(player1, List.of(drawnCard));
        addBroadsideBarrageMana();

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Burnout Bashtronaut"));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Burnout Bashtronaut");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
    }

    private void addBroadsideBarrageMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
