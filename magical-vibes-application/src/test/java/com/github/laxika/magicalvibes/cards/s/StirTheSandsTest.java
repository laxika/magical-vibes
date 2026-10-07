package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StirTheSands.class, DuneBeetle.class})
class StirTheSandsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting creates three 2/2 black Zombie tokens")
    void createsThreeZombies() {
        harness.setHand(player1, List.of(new StirTheSands()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> zombies = findPermanents(player1, "Zombie");

        assertThat(zombies).hasSize(3);
        for (Permanent zombie : zombies) {
            assertThat(zombie.getCard().getPower()).isEqualTo(2);
            assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Cycling discards, draws a card, and creates one 2/2 black Zombie token")
    void cyclingDrawsAndCreatesZombie() {
        harness.setHand(player1, List.of(new StirTheSands()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Stir the Sands");
        harness.assertInHand(player1, "Dune Beetle");

        List<Permanent> zombies = findPermanents(player1, "Zombie");
        assertThat(zombies).hasSize(1);
        assertThat(zombies.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(zombies.getFirst().getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cycling creates the Zombie before the separate draw ability resolves")
    void cyclingTokenResolvesBeforeDraw() {
        harness.setHand(player1, List.of(new StirTheSands()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Stir the Sands");
        harness.assertNotInHand(player1, "Stir the Sands");
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        harness.assertNotInHand(player1, "Dune Beetle");

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        harness.assertNotInHand(player1, "Dune Beetle");
        assertThat(harness.getGameData().stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Dune Beetle");
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }
}
