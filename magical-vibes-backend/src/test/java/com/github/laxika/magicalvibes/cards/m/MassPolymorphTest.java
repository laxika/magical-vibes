package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffect;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MassPolymorphTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Mass Polymorph has correct effect")
    void hasCorrectEffect() {
        MassPolymorph card = new MassPolymorph();

        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffect.class);
    }

    // ===== Casting =====

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
        assertThat(entry.getCard().getName()).isEqualTo("Mass Polymorph");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Exiles all creatures you control and puts revealed creature cards onto the battlefield")
    void exilesCreaturesAndRevealsNewOnes() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        // Set up library: non-creature on top, creature underneath
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new LightningBolt());
        gd.playerDecks.get(player1.getId()).add(new AirElemental());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Original creature should be exiled (not in graveyard)
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Revealed creature should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));

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
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new LightningBolt());
        gd.playerDecks.get(player1.getId()).add(new AirElemental());
        gd.playerDecks.get(player1.getId()).add(new LightningBolt());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Both original creatures should be exiled
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // Two creature cards should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

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
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new LightningBolt());
        gd.playerDecks.get(player1.getId()).add(new LightningBolt());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Original creature should be exiled
        assertThat(gd.playerExiledCards.get(player1.getId()))
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
        gd.playerDecks.get(player1.getId()).clear();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Creature should still be exiled
        assertThat(gd.playerExiledCards.get(player1.getId()))
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
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new LightningBolt());
        gd.playerDecks.get(player1.getId()).add(new AirElemental());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Both original creatures should be exiled
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // Only one creature card found, so only one on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
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
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new AirElemental());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Player 1's creature exiled
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Player 2's creature should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Mass Polymorph goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MassPolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new AirElemental());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mass Polymorph"));
    }
}
