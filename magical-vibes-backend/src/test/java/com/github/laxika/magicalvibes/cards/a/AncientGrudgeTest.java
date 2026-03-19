package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AncientGrudgeTest extends BaseCardTest {

    @Test
    @DisplayName("Ancient Grudge has correct card properties")
    void hasCorrectProperties() {
        AncientGrudge card = new AncientGrudge();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DestroyTargetPermanentEffect.class);
        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{G}");
    }

    @Test
    @DisplayName("Casting Ancient Grudge destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fountain of Youth"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));
        // Spell goes to graveyard (normal cast, not flashback)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ancient Grudge"));
    }

    @Test
    @DisplayName("Cannot target a creature with Ancient Grudge")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback from graveyard destroys target artifact")
    void flashbackDestroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fountain of Youth"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving, not sent to graveyard")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Should NOT be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Ancient Grudge"));
        // Should be in exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ancient Grudge"));
    }

    @Test
    @DisplayName("Flashback spell is exiled when it fizzles")
    void flashbackExilesOnFizzle() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castFlashback(player1, 0, targetId);

        // Remove the target before resolution to cause fizzle
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Should NOT be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Ancient Grudge"));
        // Should be in exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ancient Grudge"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack as instant spell")
    void flashbackPutsOnStackAsSpell() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
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
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        // Only add green mana (flashback cost is {G}, not {1}{R})
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castFlashback(player1, 0, targetId);

        // Mana should be consumed
        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        // No mana added

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback removes card from graveyard when cast")
    void flashbackRemovesFromGraveyard() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castFlashback(player1, 0, targetId);

        // Card should no longer be in the graveyard
        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Ancient Grudge"));
    }
}
