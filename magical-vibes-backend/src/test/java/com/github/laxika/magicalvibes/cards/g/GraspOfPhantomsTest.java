package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
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

class GraspOfPhantomsTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Grasp of Phantoms has correct effects")
    void hasCorrectEffects() {
        GraspOfPhantoms card = new GraspOfPhantoms();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(PutTargetOnTopOfLibraryEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Grasp of Phantoms targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new GraspOfPhantoms()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Grasp of Phantoms");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    // ===== Targeting restrictions =====

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID landId = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new GraspOfPhantoms()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving puts creature on top of owner's library")
    void resolvingPutsCreatureOnTopOfLibrary() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new GraspOfPhantoms()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature removed from battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Creature NOT in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // Creature on top of library (first element)
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Grasp of Phantoms goes to graveyard =====

    @Test
    @DisplayName("Grasp of Phantoms goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new GraspOfPhantoms()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grasp of Phantoms"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new GraspOfPhantoms()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Library should be unchanged
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Grasp of Phantoms still goes to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grasp of Phantoms"));
    }

    // ===== Flashback =====

    @Test
    @DisplayName("Flashback from graveyard puts creature on top of library")
    void flashbackFromGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setGraveyard(player1, List.of(new GraspOfPhantoms()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature removed from battlefield and on top of library
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Flashback exiles the spell after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setGraveyard(player1, List.of(new GraspOfPhantoms()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Should NOT be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grasp of Phantoms"));
        // Should be in exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grasp of Phantoms"));
    }
}
