package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MercilessEvictionTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode exiles every artifact and nothing else")
    void exilesAllArtifacts() {
        setUpBoard();
        castMode(0);

        harness.assertNotOnBattlefield(player1, "Pithing Needle");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Pithing Needle"));
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Ghostly Prison");
        harness.assertOnBattlefield(player2, "Chandra Nalaar");
    }

    @Test
    @DisplayName("Creature mode exiles every creature and nothing else")
    void exilesAllCreatures() {
        setUpBoard();
        castMode(1);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertOnBattlefield(player1, "Pithing Needle");
        harness.assertOnBattlefield(player2, "Chandra Nalaar");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Enchantment mode exiles every enchantment and nothing else")
    void exilesAllEnchantments() {
        setUpBoard();
        castMode(2);

        harness.assertNotOnBattlefield(player2, "Ghostly Prison");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Ghostly Prison"));
        harness.assertOnBattlefield(player1, "Pithing Needle");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Chandra Nalaar");
    }

    @Test
    @DisplayName("Planeswalker mode exiles every planeswalker and nothing else")
    void exilesAllPlaneswalkers() {
        setUpBoard();
        castMode(3);

        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Chandra Nalaar"));
        harness.assertOnBattlefield(player1, "Pithing Needle");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Ghostly Prison");
    }

    private void setUpBoard() {
        harness.addToBattlefield(player1, new PithingNeedle());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GhostlyPrison());
        harness.addToBattlefieldAndReturn(player2, new ChandraNalaar())
                .setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new MercilessEviction()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);
    }

    private void castMode(int modeIndex) {
        harness.castSorcery(player1, 0, modeIndex);
        harness.passBothPriorities();
    }
}
