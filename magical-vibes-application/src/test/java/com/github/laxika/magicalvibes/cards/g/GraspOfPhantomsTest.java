package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraspOfPhantoms.class, WalkingCorpse.class, Forest.class})
class GraspOfPhantomsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Grasp of Phantoms targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");

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

    @Test
    @DisplayName("Resolving puts creature on top of owner's library")
    void resolvingPutsCreatureOnTopOfLibrary() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new GraspOfPhantoms()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        // Creature removed from battlefield
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        // Creature NOT in graveyard
        harness.assertNotInGraveyard(player2, "Walking Corpse");
        // Creature on top of library (first element)
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Walking Corpse");
    }

    @Test
    @DisplayName("Grasp of Phantoms goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");

        harness.setHand(player1, List.of(new GraspOfPhantoms()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grasp of Phantoms");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");

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
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Grasp of Phantoms still goes to graveyard
        harness.assertInGraveyard(player1, "Grasp of Phantoms");
    }

    @Test
    @DisplayName("Flashback from graveyard puts creature on top of library")
    void flashbackFromGraveyard() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setGraveyard(player1, List.of(new GraspOfPhantoms()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveFlashback(player1, 0, targetId);

        GameData gd = harness.getGameData();
        // Creature removed from battlefield and on top of library
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Walking Corpse");
    }

    @Test
    @DisplayName("A creature controlled by another player returns to its owner's library")
    void returnsCreatureToOwnersLibrary() {
        WalkingCorpse creature = new WalkingCorpse();
        creature.setOwnerId(player1.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, creature).getId();
        int controllerDeckSize = harness.getGameData().playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new GraspOfPhantoms()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        assertThat(harness.getGameData().playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
        assertThat(harness.getGameData().playerDecks.get(player2.getId())).hasSize(controllerDeckSize);
    }

    @Test
    @DisplayName("Flashback still exiles the spell when its target becomes illegal")
    void flashbackExilesWithIllegalTarget() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse()).getId();
        int deckSize = harness.getGameData().playerDecks.get(player2.getId()).size();
        GraspOfPhantoms spell = new GraspOfPhantoms();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castFlashback(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerDecks.get(player2.getId())).hasSize(deckSize);
        harness.assertNotInGraveyard(player1, "Grasp of Phantoms");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Flashback cannot be paid using the cheaper normal mana cost")
    void flashbackRequiresFullCost() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse()).getId();
        harness.setGraveyard(player1, List.of(new GraspOfPhantoms()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Grasp of Phantoms");
        harness.assertOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Flashback exiles the spell after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");

        harness.setGraveyard(player1, List.of(new GraspOfPhantoms()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveFlashback(player1, 0, targetId);

        GameData gd = harness.getGameData();
        // Should NOT be in graveyard
        harness.assertNotInGraveyard(player1, "Grasp of Phantoms");
        // Should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grasp of Phantoms"));
    }
}
