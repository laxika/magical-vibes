package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({AncientGrudge.class, PrismaticLens.class, AshcoatBear.class, Cancel.class})
class AncientGrudgeTest extends BaseCardTest {

    @Test
    @DisplayName("A countered flashback Ancient Grudge is exiled and leaves its target intact")
    void counteredFlashbackIsExiled() {
        AncientGrudge grudge = new AncientGrudge();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PrismaticLens()).getId();
        harness.setGraveyard(player1, List.of(grudge));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFlashback(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, grudge.getId());

        harness.assertOnBattlefield(player2, "Prismatic Lens");
        harness.assertNotInGraveyard(player1, "Ancient Grudge");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(grudge.getId()));
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The same Ancient Grudge can destroy an artifact from hand and another with flashback")
    void normalCastThenFlashback() {
        AncientGrudge grudge = new AncientGrudge();
        UUID ownArtifactId = harness.addToBattlefieldAndReturn(player1, new PrismaticLens()).getId();
        UUID opposingArtifactId = harness.addToBattlefieldAndReturn(player2, new PrismaticLens()).getId();
        harness.setHand(player1, List.of(grudge));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, ownArtifactId);

        harness.assertInGraveyard(player1, "Prismatic Lens");
        harness.assertInGraveyard(player1, "Ancient Grudge");
        harness.assertOnBattlefield(player2, "Prismatic Lens");
        int graveyardIndex = harness.getGameData().playerGraveyards.get(player1.getId()).indexOf(grudge);
        assertThat(graveyardIndex).isGreaterThanOrEqualTo(0);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFlashback(player1, graveyardIndex, opposingArtifactId);

        harness.assertNotOnBattlefield(player1, "Prismatic Lens");
        harness.assertNotOnBattlefield(player2, "Prismatic Lens");
        harness.assertInGraveyard(player2, "Prismatic Lens");
        harness.assertNotInGraveyard(player1, "Ancient Grudge");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(grudge.getId()));
    }

    @Test
    @DisplayName("Red mana cannot pay Ancient Grudge's green flashback cost")
    void flashbackRejectsWrongColorMana() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PrismaticLens()).getId();
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Ancient Grudge");
        harness.assertOnBattlefield(player2, "Prismatic Lens");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Casting Ancient Grudge destroys target artifact")
    void destroysArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PrismaticLens()).getId();
        harness.setHand(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Prismatic Lens");
        harness.assertInGraveyard(player2, "Prismatic Lens");
        // Spell goes to graveyard (normal cast, not flashback)
        harness.assertInGraveyard(player1, "Ancient Grudge");
    }

    @Test
    @DisplayName("Cannot target a creature with Ancient Grudge")
    void cannotTargetCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new AshcoatBear()).getId();
        harness.setHand(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature when casting Ancient Grudge with flashback")
    void cannotFlashbackTargetCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new AshcoatBear()).getId();
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback from graveyard destroys target artifact")
    void flashbackDestroysArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PrismaticLens()).getId();
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFlashback(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Prismatic Lens");
        harness.assertInGraveyard(player2, "Prismatic Lens");
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving, not sent to graveyard")
    void flashbackExilesAfterResolving() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PrismaticLens()).getId();
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFlashback(player1, 0, targetId);

        GameData gd = harness.getGameData();
        // Should NOT be in graveyard
        harness.assertNotInGraveyard(player1, "Ancient Grudge");
        // Should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ancient Grudge"));
    }

    @Test
    @DisplayName("Flashback spell is exiled when it fizzles")
    void flashbackExilesOnFizzle() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PrismaticLens()).getId();
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, targetId);

        // Remove the target before resolution to cause fizzle
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Should NOT be in graveyard
        harness.assertNotInGraveyard(player1, "Ancient Grudge");
        // Should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ancient Grudge"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack as instant spell")
    void flashbackPutsOnStackAsSpell() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PrismaticLens()).getId();
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Ancient Grudge");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
        assertThat(entry.isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Flashback pays the flashback cost, not the mana cost")
    void flashbackPaysFlashbackCost() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PrismaticLens()).getId();
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        // Only add green mana (flashback cost is {G}, not {1}{R})
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, targetId);

        // Mana should be consumed
        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PrismaticLens()).getId();
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        // No mana added

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback removes card from graveyard when cast")
    void flashbackRemovesFromGraveyard() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PrismaticLens()).getId();
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, targetId);

        // Card should no longer be in the graveyard
        harness.assertNotInGraveyard(player1, "Ancient Grudge");
    }
}
