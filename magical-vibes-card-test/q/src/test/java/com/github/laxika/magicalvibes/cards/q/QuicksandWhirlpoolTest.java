package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MalametBrawler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuicksandWhirlpool.class, MalametBrawler.class, Forest.class})
class QuicksandWhirlpoolTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a tapped creature for the reduced cost")
    void exilesTappedCreatureForReducedCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MalametBrawler());
        target.tap();

        harness.setHand(player1, List.of(new QuicksandWhirlpool()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Exiles an untapped creature for the full cost")
    void exilesUntappedCreatureForFullCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MalametBrawler());

        harness.setHand(player1, List.of(new QuicksandWhirlpool()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new QuicksandWhirlpool()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPayReducedCostForUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MalametBrawler());
        harness.setHand(player1, List.of(new QuicksandWhirlpool()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void reductionDoesNotRemoveWhiteManaRequirement() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MalametBrawler());
        target.tap();
        harness.setHand(player1, List.of(new QuicksandWhirlpool()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillExilesTargetThatUntapsAfterCasting() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MalametBrawler());
        target.tap();
        harness.setHand(player1, List.of(new QuicksandWhirlpool()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void canExileOwnTappedCreatureForReducedCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        target.tap();
        harness.setHand(player1, List.of(new QuicksandWhirlpool()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }
}
