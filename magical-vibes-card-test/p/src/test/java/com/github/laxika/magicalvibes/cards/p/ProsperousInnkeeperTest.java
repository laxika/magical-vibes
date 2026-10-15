package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.cards.y.YouSeeAPairOfGoblins;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProsperousInnkeeper.class, DireWolfProwler.class, YouSeeAPairOfGoblins.class})
class ProsperousInnkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("When Prosperous Innkeeper enters, it creates a Treasure token")
    void etbCreatesTreasureToken() {
        harness.setHand(player1, List.of(new ProsperousInnkeeper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Gains 1 life when another creature you control enters")
    void gainsLifeWhenAnotherCreatureEnters() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ProsperousInnkeeper());
        harness.setHand(player1, List.of(new DireWolfProwler()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not gain life when Prosperous Innkeeper itself enters")
    void doesNotGainLifeOnOwnEntry() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ProsperousInnkeeper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not gain life when an opponent's creature enters")
    void doesNotGainLifeOnOpponentCreatureEntry() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ProsperousInnkeeper());
        harness.enterBattlefieldAndReturn(player2, new DireWolfProwler());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Life is gained only when the creature-entry trigger resolves")
    void lifeGainWaitsForTriggerResolution() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ProsperousInnkeeper());

        harness.enterBattlefieldAndReturn(player1, new DireWolfProwler());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("A second Innkeeper triggers the first but not itself or the Treasure")
    void secondInnkeeperGainsOneLifeAndCreatesTreasure() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ProsperousInnkeeper());
        harness.setHand(player1, List.of(new ProsperousInnkeeper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Each creature token entering triggers each Innkeeper")
    void creatureTokensTriggerEachInnkeeper() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ProsperousInnkeeper());
        harness.addToBattlefield(player1, new ProsperousInnkeeper());
        harness.setHand(player1, List.of(new YouSeeAPairOfGoblins()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalInstant(player1, 0, 1, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }
}
