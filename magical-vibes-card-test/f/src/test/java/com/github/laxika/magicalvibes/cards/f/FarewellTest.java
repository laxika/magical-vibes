package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AutomatedArtificer;
import com.github.laxika.magicalvibes.cards.g.GoldenTailDisciple;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.ShortCircuit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Farewell.class, PithingNeedle.class, GrizzlyBears.class, GhostlyPrison.class,
        Forest.class, Shock.class, LlanowarElves.class, AutomatedArtificer.class,
        GoldenTailDisciple.class, ShortCircuit.class})
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

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).containsExactly("Farewell");
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
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).containsExactly("Farewell");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(ownCard)
                .anyMatch(card -> card.getName().equals("Pithing Needle"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(opponentsCard)
                .anyMatch(card -> card.getName().equals("Ghostly Prison"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("An Aura remains available for the enchantment mode after its host is exiled")
    void exilesAuraWithLaterEnchantmentMode(int hostMode) {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new AutomatedArtificer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShortCircuit());
        aura.setAttachedTo(host.getId());

        cast(new int[]{2, hostMode});

        harness.assertNotOnBattlefield(player2, "Automated Artificer");
        harness.assertNotOnBattlefield(player1, "Short Circuit");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(host.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(aura.getCard());
        harness.assertNotInGraveyard(player1, "Short Circuit");
    }

    @Test
    @DisplayName("An orphaned Aura goes to the graveyard only after the graveyard mode finishes")
    void orphanedAuraIsNotExiledByGraveyardMode() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new AutomatedArtificer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShortCircuit());
        aura.setAttachedTo(host.getId());
        Card graveyardCard = new GoldenTailDisciple();
        harness.setGraveyard(player1, List.of(graveyardCard));

        cast(new int[]{3, 1});

        harness.assertNotOnBattlefield(player2, "Automated Artificer");
        harness.assertNotOnBattlefield(player1, "Short Circuit");
        harness.assertInGraveyard(player1, "Short Circuit");
        harness.assertInGraveyard(player1, "Farewell");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(host.getCard());
    }

    @Test
    @DisplayName("All four modes exile both players' matching permanents and graveyards while leaving lands")
    void exilesAllSelectedTypesAndGraveyards() {
        Card ownArtifactCreature = new AutomatedArtificer();
        Card opposingArtifactCreature = new AutomatedArtificer();
        Card ownEnchantmentCreature = new GoldenTailDisciple();
        Card opposingEnchantmentCreature = new GoldenTailDisciple();
        Card ownGraveyardCard = new ShortCircuit();
        Card opposingGraveyardCard = new ShortCircuit();
        harness.addToBattlefield(player1, ownArtifactCreature);
        harness.addToBattlefield(player2, opposingArtifactCreature);
        harness.addToBattlefield(player1, ownEnchantmentCreature);
        harness.addToBattlefield(player2, opposingEnchantmentCreature);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setGraveyard(player1, List.of(ownGraveyardCard));
        harness.setGraveyard(player2, List.of(opposingGraveyardCard));

        cast(new int[]{3, 2, 1, 0});

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(
                ownArtifactCreature, ownEnchantmentCreature, ownGraveyardCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(
                opposingArtifactCreature, opposingEnchantmentCreature, opposingGraveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).containsExactly("Farewell");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("All modes can resolve with empty battlefields and graveyards")
    void resolvesWithNoMatchingObjects() {
        cast(new int[]{0, 1, 2, 3});

        harness.assertInGraveyard(player1, "Farewell");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
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
