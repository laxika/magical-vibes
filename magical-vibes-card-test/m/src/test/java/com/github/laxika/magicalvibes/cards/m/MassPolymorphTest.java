package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AwakenerDruid;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MassPolymorph.class, AirElemental.class, AwakenerDruid.class, Forest.class,
        GrizzlyBears.class, LightningBolt.class, LlanowarElves.class, ObstinateBaloth.class})
class MassPolymorphTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Mass Polymorph puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(MassPolymorph.class);
    }

    @Test
    @DisplayName("Exiles all creatures you control and puts revealed creature cards onto the battlefield")
    void exilesCreaturesAndRevealsNewOnes() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        // Set up library: non-creature on top, creature underneath
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of(new LightningBolt(), new AirElemental()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Original creature should be exiled (not in graveyard)
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");

        // Revealed creature should be on the battlefield
        harness.assertOnBattlefield(player1, "Air Elemental");

        // Non-creature card should be shuffled back into library
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Exiling multiple creatures reveals that many creature cards")
    void multipleCreaturesRevealMultiple() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        // Set up library: non-creature, creature, non-creature, creature
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of(new LightningBolt(), new AirElemental(), new LightningBolt(), new GrizzlyBears()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Both original creatures should be exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // Two creature cards should be on the battlefield
        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Non-creature cards should be back in library
        long boltsInLibrary = gd.playerDecks.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Lightning Bolt"))
                .count();
        assertThat(boltsInLibrary).isEqualTo(2);
    }

    @Test
    @DisplayName("No creatures controlled — nothing happens")
    void noCreaturesControlled() {
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Library should be untouched (same size)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        // Nothing on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No creature cards in library — all revealed cards shuffled back")
    void noCreaturesInLibrary() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        // Library with only non-creature cards
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Original creature should be exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // No creatures on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        // Non-creature cards should be shuffled back into library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Empty library — creatures are exiled but no cards are revealed")
    void emptyLibrary() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Creature should still be exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Library should still be empty
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        // No creatures on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Fewer creature cards in library than creatures exiled — puts whatever is found")
    void fewerCreaturesInLibraryThanExiled() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        // Library with only one creature card (but two were exiled)
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of(new LightningBolt(), new AirElemental()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Both original creatures should be exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // Only one creature card found, so only one on battlefield
        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        // Non-creature cards shuffled back
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Does not exile opponent's creatures")
    void doesNotExileOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of(new AirElemental()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Player 1's creature exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Player 2's creature should still be on the battlefield
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Mass Polymorph goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of(new AirElemental()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mass Polymorph");
    }

    @Test
    @DisplayName("Exiles animated lands and counts them among the creatures exiled")
    void exilesAnimatedForestAlongsideItsDruid() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, harness.getPermanentId(player1, "Forest"));
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Forest"))).isTrue();

        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.setLibrary(player1, List.of(new LlanowarElves(), new ObstinateBaloth()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName()).contains("Forest", "Awakener Druid");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Obstinate Baloth");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Stops revealing at the required creature and leaves later creatures in the library")
    void stopsAfterRequiredCreature() {
        harness.addToBattlefield(player1, new LlanowarElves());
        AirElemental revealed = new AirElemental();
        GrizzlyBears unrevealed = new GrizzlyBears();
        LightningBolt noncreature = new LightningBolt();
        harness.setLibrary(player1, List.of(noncreature, revealed, unrevealed));
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(noncreature, unrevealed);
    }

    @Test
    @DisplayName("Returned noncreatures are shuffled before revealed creatures' enter triggers resolve")
    void enterTriggerWaitsUntilSpellFinishes() {
        harness.addToBattlefield(player1, new LlanowarElves());
        LightningBolt noncreature = new LightningBolt();
        harness.setLibrary(player1, List.of(noncreature, new ObstinateBaloth()));
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Obstinate Baloth");
        harness.assertInGraveyard(player1, "Mass Polymorph");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(noncreature);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertLife(player1, 24);
    }
}
