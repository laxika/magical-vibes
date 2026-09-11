package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Farewell.class, PithingNeedle.class, GrizzlyBears.class, GhostlyPrison.class,
        Forest.class, Shock.class, LlanowarElves.class})
class FarewellTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode exiles all artifacts and leaves other permanents")
    void exilesAllArtifacts() {
        addBattlefieldPermanents();

        cast(new int[]{0});

        harness.assertNotOnBattlefield(player1, "Pithing Needle");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Pithing Needle"));
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Ghostly Prison");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Creature mode exiles all creatures and leaves other permanents")
    void exilesAllCreatures() {
        addBattlefieldPermanents();

        cast(new int[]{1});

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertOnBattlefield(player1, "Pithing Needle");
        harness.assertOnBattlefield(player2, "Ghostly Prison");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Enchantment mode exiles all enchantments and leaves other permanents")
    void exilesAllEnchantments() {
        addBattlefieldPermanents();

        cast(new int[]{2});

        harness.assertNotOnBattlefield(player2, "Ghostly Prison");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Ghostly Prison"));
        harness.assertOnBattlefield(player1, "Pithing Needle");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Graveyard mode exiles all cards from all graveyards")
    void exilesAllGraveyards() {
        Card ownCard = new Shock();
        Card opponentsCard = new LlanowarElves();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentsCard));

        cast(new int[]{3});

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentsCard);
    }

    @Test
    @DisplayName("Choosing multiple modes exiles each selected category")
    void exilesMultipleSelectedCategories() {
        addBattlefieldPermanents();
        Card ownCard = new Shock();
        Card opponentsCard = new LlanowarElves();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentsCard));

        cast(new int[]{0, 2, 3});

        harness.assertNotOnBattlefield(player1, "Pithing Needle");
        harness.assertNotOnBattlefield(player2, "Ghostly Prison");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(ownCard)
                .anyMatch(card -> card.getName().equals("Pithing Needle"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(opponentsCard)
                .anyMatch(card -> card.getName().equals("Ghostly Prison"));
    }

    private void addBattlefieldPermanents() {
        harness.addToBattlefield(player1, new PithingNeedle());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GhostlyPrison());
        harness.addToBattlefield(player1, new Forest());
    }

    private void cast(int[] modes) {
        harness.setHand(player1, List.of(new Farewell()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castModalSorceryWithModes(player1, 0, 1, 4, modes, List.of(), null);
        harness.passBothPriorities();
    }
}
