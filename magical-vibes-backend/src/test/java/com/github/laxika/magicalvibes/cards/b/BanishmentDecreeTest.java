package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BanishmentDecreeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Banishment Decree has correct effects")
    void hasCorrectEffects() {
        BanishmentDecree card = new BanishmentDecree();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(PutTargetOnTopOfLibraryEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Banishment Decree targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

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
            harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.f.Forest());
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.setHand(player2, List.of(new BanishmentDecree()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature removed from battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Creature NOT in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // Creature on top of library (first element)
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Resolving against artifacts =====

    @Test
    @DisplayName("Resolving puts artifact on top of owner's library")
    void resolvingPutsArtifactOnTopOfLibrary() {
        harness.addToBattlefield(player1, new Ornithopter());
        UUID targetId = harness.getPermanentId(player1, "Ornithopter");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.setHand(player2, List.of(new BanishmentDecree()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ornithopter"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Ornithopter"));
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Ornithopter");
    }

    // ===== Resolving against enchantments =====

    @Test
    @DisplayName("Resolving puts enchantment on top of owner's library")
    void resolvingPutsEnchantmentOnTopOfLibrary() {
        // Put Pacifism on the battlefield attached to a creature
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new Pacifism());
        UUID pacifismId = harness.getPermanentId(player2, "Pacifism");

        // Attach Pacifism to Grizzly Bears
        GameData gd = harness.getGameData();
        Permanent pacifism = gqs.findPermanentById(gd, pacifismId);
        pacifism.setAttachedTo(bearsId);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new BanishmentDecree()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, pacifismId);
        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pacifism"));
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Pacifism");
    }

    // ===== Banishment Decree goes to graveyard =====

    @Test
    @DisplayName("Banishment Decree goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new BanishmentDecree()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Banishment Decree"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

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
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Banishment Decree still goes to graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Banishment Decree"));
    }
}
