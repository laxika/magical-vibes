package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SphereOfTheSuns;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanishmentDecree.class, GrizzlyBears.class, Ornithopter.class, Pacifism.class, Forest.class, SphereOfTheSuns.class})
class BanishmentDecreeTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Banishment Decree targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.setHand(player2, List.of(new BanishmentDecree()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Banishment Decree");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    // ===== Targeting restrictions =====

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        // Player1 has a land on the battlefield (from setup)
        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID landId = null;
        for (Permanent p : bf) {
            if (p.getCard().getType() == com.github.laxika.magicalvibes.model.CardType.LAND) {
                landId = p.getId();
                break;
            }
        }
        // If no land exists, add one manually
        if (landId == null) {
            harness.addToBattlefield(player1, new Forest());
            landId = harness.getPermanentId(player1, "Forest");
        }

        harness.setHand(player2, List.of(new BanishmentDecree()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        UUID finalLandId = landId;
        assertThatThrownBy(() -> harness.castInstant(player2, 0, finalLandId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving against creatures =====

    @Test
    @DisplayName("Resolving puts creature on top of owner's library")
    void resolvingPutsCreatureOnTopOfLibrary() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.setHand(player2, List.of(new BanishmentDecree()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, targetId);

        GameData gd = harness.getGameData();
        // Creature removed from battlefield
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        // Creature NOT in graveyard
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        // Creature on top of library (first element)
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Resolving against artifacts =====

    @Test
    @DisplayName("Resolving puts artifact on top of owner's library")
    void resolvingPutsArtifactOnTopOfLibrary() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Ornithopter()).getId();

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.setHand(player2, List.of(new BanishmentDecree()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Ornithopter");
    }

    // ===== Resolving against enchantments =====

    @Test
    @DisplayName("Resolving puts enchantment on top of owner's library")
    void resolvingPutsEnchantmentOnTopOfLibrary() {
        // Put Pacifism on the battlefield attached to a creature
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID pacifismId = harness.addToBattlefieldAndReturn(player2, new Pacifism()).getId();

        // Attach Pacifism to Grizzly Bears
        GameData gd = harness.getGameData();
        Permanent pacifism = gqs.findPermanentById(gd, pacifismId);
        pacifism.setAttachedTo(bearsId);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new BanishmentDecree()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveInstant(player1, 0, pacifismId);

        gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Pacifism");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Pacifism");
    }

    // ===== Banishment Decree goes to graveyard =====

    @Test
    @DisplayName("Banishment Decree goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.setHand(player2, List.of(new BanishmentDecree()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Banishment Decree");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.setHand(player2, List.of(new BanishmentDecree()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Library should be unchanged
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Banishment Decree still goes to graveyard
        harness.assertInGraveyard(player2, "Banishment Decree");
    }

    @Test
    @DisplayName("A noncreature artifact is a legal target")
    void putsNoncreatureArtifactOnTopOfLibrary() {
        SphereOfTheSuns sphere = new SphereOfTheSuns();
        Permanent target = harness.addToBattlefieldAndReturn(player2, sphere);
        harness.setHand(player1, List.of(new BanishmentDecree()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Sphere of the Suns");
        assertThat(harness.getGameData().playerDecks.get(player2.getId()).getFirst()).isSameAs(sphere);
        harness.assertNotInGraveyard(player2, "Sphere of the Suns");
    }

    @Test
    @DisplayName("A stolen creature goes to its owner's library rather than its controller's")
    void putsStolenCreatureInOwnersLibrary() {
        GrizzlyBears bears = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, bears);
        GameData gd = harness.getGameData();
        gd.stolenCreatures.put(target.getId(), player1.getId());
        List<Card> controllerLibraryBefore = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.setHand(player1, List.of(new BanishmentDecree()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(controllerLibraryBefore);
    }

    @Test
    @DisplayName("Tucking an enchanted creature puts its Aura in the graveyard")
    void attachedAuraGoesToGraveyardWhenCreatureLeaves() {
        GrizzlyBears bears = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, bears);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(target.getId());
        harness.setHand(player1, List.of(new BanishmentDecree()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(harness.getGameData().playerDecks.get(player2.getId()).getFirst()).isSameAs(bears);
        harness.assertNotOnBattlefield(player2, "Pacifism");
        harness.assertInGraveyard(player2, "Pacifism");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }
}
