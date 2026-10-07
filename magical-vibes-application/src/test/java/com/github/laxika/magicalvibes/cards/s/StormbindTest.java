package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Stormbind.class, BalduvianBears.class, Forest.class, ChandraNalaar.class})
class StormbindTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 2 damage to a creature and discards a card at random as a cost")
    void damagesCreatureAndDiscardsAtRandom() {
        harness.addToBattlefield(player1, new Stormbind());
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bearId = harness.getPermanentId(player2, "Balduvian Bears");
        harness.activateAbility(player1, battlefieldIndex(player1, "Stormbind"), null, bearId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Balduvian Bears");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Ability deals 2 damage to a player")
    void damagesPlayer() {
        harness.addToBattlefield(player1, new Stormbind());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Stormbind"), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Ability deals 2 damage to a planeswalker")
    void damagesPlaneswalker() {
        harness.addToBattlefield(player1, new Stormbind());
        ChandraNalaar chandra = new ChandraNalaar();
        chandra.setLoyalty(6);
        Permanent target = harness.addToBattlefieldAndReturn(player2, chandra);
        target.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Stormbind"), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Pays the random discard immediately when the ability is activated")
    void paysRandomDiscardAsActivationCost() {
        harness.addToBattlefield(player1, new Stormbind());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Stormbind"), null, player2.getId());

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new Stormbind());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Stormbind"), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana for the generic cost")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new Stormbind());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Stormbind"), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Ability can be activated more than once without tapping")
    void canBeActivatedMoreThanOnceWithoutTapping() {
        harness.addToBattlefield(player1, new Stormbind());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int stormbindIndex = battlefieldIndex(player1, "Stormbind");
        harness.activateAbility(player1, stormbindIndex, null, player2.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, stormbindIndex, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate with an empty hand (no card to discard)")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new Stormbind());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Stormbind"), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Random discard removes exactly one of the actual cards in hand")
    void discardsExactlyOneCardFromMixedHand() {
        harness.addToBattlefield(player1, new Stormbind());
        Forest forest = new Forest();
        BalduvianBears bears = new BalduvianBears();
        harness.setHand(player1, List.of(forest, bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Stormbind"), null, player2.getId());

        List<Card> hand = gd.playerHands.get(player1.getId());
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(graveyard).hasSize(1);
        assertThat(hand.getFirst()).isIn(forest, bears);
        assertThat(graveyard.getFirst()).isIn(forest, bears).isNotSameAs(hand.getFirst());

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(hand).hasSize(1);
        assertThat(graveyard).hasSize(1);
    }

    @Test
    @DisplayName("Ability still deals damage after Stormbind leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent stormbind = harness.addToBattlefieldAndReturn(player1, new Stormbind());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Stormbind"), null, player2.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, stormbind);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Stormbind");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Discard cost remains paid when the target leaves and returns before resolution")
    void returnedCreatureIsNotTheOriginalTarget() {
        harness.addToBattlefield(player1, new Stormbind());
        BalduvianBears bears = new BalduvianBears();
        Permanent original = harness.addToBattlefieldAndReturn(player2, bears);
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Stormbind"), null, original.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, original);
        harness.setHand(player2, List.of());
        Permanent returned = harness.addToBattlefieldAndReturn(player2, bears);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Balduvian Bears");
        assertThat(returned.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
    }

    private int battlefieldIndex(Player player, String cardName) {
        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(cardName)) {
                return i;
            }
        }
        throw new IllegalStateException("Permanent not found: " + cardName);
    }
}
