package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BorosCharm;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MercilessEviction.class, ChandraNalaar.class, Forest.class, GrizzlyBears.class,
        GhostlyPrison.class, PithingNeedle.class, MillennialGargoyle.class, BorosCharm.class})
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

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Artifact and creature modes each exile artifact creatures controlled by both players")
    void exilesArtifactCreaturesOnBothSides(int modeIndex) {
        harness.addToBattlefield(player1, new MillennialGargoyle());
        harness.addToBattlefield(player1, new MillennialGargoyle());
        harness.addToBattlefield(player2, new MillennialGargoyle());
        harness.setHand(player1, List.of(new MercilessEviction()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);

        castMode(modeIndex);

        harness.assertNotOnBattlefield(player1, "Millennial Gargoyle");
        harness.assertNotOnBattlefield(player2, "Millennial Gargoyle");
        harness.assertNotInGraveyard(player1, "Millennial Gargoyle");
        harness.assertNotInGraveyard(player2, "Millennial Gargoyle");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(c -> c.getName().equals("Millennial Gargoyle")).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Millennial Gargoyle")).hasSize(1);
        harness.assertInGraveyard(player1, "Merciless Eviction");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    @DisplayName("Every mode can resolve on an empty battlefield and leaves cards in other zones alone")
    void resolvesWithoutMatchingPermanents(int modeIndex) {
        harness.setHand(player1, List.of(new MercilessEviction(), new MillennialGargoyle()));
        harness.setGraveyard(player2, List.of(new MillennialGargoyle()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);

        castMode(modeIndex);

        harness.assertInHand(player1, "Millennial Gargoyle");
        harness.assertInGraveyard(player2, "Millennial Gargoyle");
        harness.assertInGraveyard(player1, "Merciless Eviction");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Exile removes indestructible artifact creatures")
    void exilesIndestructiblePermanents(int modeIndex) {
        var gargoyle = harness.addToBattlefieldAndReturn(player1, new MillennialGargoyle());
        harness.setHand(player1, List.of(new BorosCharm(), new MercilessEviction()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();
        assertThat(gargoyle.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        castMode(modeIndex);

        harness.assertNotOnBattlefield(player1, "Millennial Gargoyle");
        harness.assertNotInGraveyard(player1, "Millennial Gargoyle");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Millennial Gargoyle"));
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
        harness.castAndResolveSorcery(player1, 0, modeIndex);
    }
}
