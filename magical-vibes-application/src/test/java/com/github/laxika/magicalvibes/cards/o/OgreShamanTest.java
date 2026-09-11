package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CinderCrawler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreShaman.class, CinderCrawler.class})
class OgreShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 2 damage to a creature and discards a card at random as a cost")
    void damagesCreatureAndDiscardsAtRandom() {
        addOgreShaman();
        harness.addToBattlefield(player2, new CinderCrawler());
        harness.setHand(player1, List.of(new CinderCrawler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID crawlerId = harness.getPermanentId(player2, "Cinder Crawler");
        harness.activateAbility(player1, 0, null, crawlerId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cinder Crawler");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Cinder Crawler");
    }

    @Test
    @DisplayName("Ability deals 2 damage to a player")
    void damagesPlayer() {
        addOgreShaman();
        harness.setHand(player1, List.of(new CinderCrawler()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Cinder Crawler");
    }

    @Test
    @DisplayName("Cannot activate with an empty hand")
    void cannotActivateWithEmptyHand() {
        addOgreShaman();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pays one random discard immediately when the ability is activated")
    void paysRandomDiscardAsActivationCost() {
        addOgreShaman();
        harness.setHand(player1, List.of(new CinderCrawler(), new CinderCrawler()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot activate without paying the generic mana cost")
    void cannotActivateWithoutEnoughMana() {
        addOgreShaman();
        harness.setHand(player1, List.of(new CinderCrawler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Cinder Crawler");
        harness.assertNotInGraveyard(player1, "Cinder Crawler");
    }

    private void addOgreShaman() {
        harness.addToBattlefield(player1, new OgreShaman());
    }
}
