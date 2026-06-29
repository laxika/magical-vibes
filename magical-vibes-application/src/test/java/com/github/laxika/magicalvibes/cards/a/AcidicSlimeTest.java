package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
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

class AcidicSlimeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Acidic Slime has correct card properties")
    void hasCorrectProperties() {
        AcidicSlime card = new AcidicSlime();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(DestroyTargetPermanentEffect.class);
    }

    // ===== ETB destroys artifact =====

    @Test
    @DisplayName("ETB destroys target artifact")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Acidic Slime");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    // ===== ETB destroys enchantment =====

    @Test
    @DisplayName("ETB destroys target enchantment")
    void etbDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Acidic Slime");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    // ===== ETB destroys land =====

    @Test
    @DisplayName("ETB destroys target land")
    void etbDestroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Acidic Slime");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a creature that is not an artifact, enchantment, or land")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or land");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("Can cast without a target when no valid targets on battlefield")
    void canCastWithoutTargetWhenNoValidTargets() {
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Acidic Slime");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Acidic Slime");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can cast without target when only creatures exist")
    void canCastWithoutTargetWhenOnlyCreaturesExist() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AcidicSlime()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Acidic Slime");
    }
}
