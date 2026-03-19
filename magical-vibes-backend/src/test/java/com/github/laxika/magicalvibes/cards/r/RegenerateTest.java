package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegenerateTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerate has correct card properties")
    void hasCorrectCardProperties() {
        Regenerate card = new Regenerate();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.SPELL).getFirst())
                .isInstanceOf(RegenerateEffect.class);
        RegenerateEffect effect = (RegenerateEffect) card.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.SPELL).getFirst();
        assertThat(effect.targetsPermanent()).isTrue();
    }

    @Test
    @DisplayName("Casting Regenerate puts it on the stack targeting a creature")
    void castingPutsItOnStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Regenerate");
        assertThat(entry.getTargetId()).isEqualTo(bearId);
    }

    @Test
    @DisplayName("Resolving Regenerate grants regeneration shield to target creature")
    void resolvingGrantsRegenerationShield() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield from Regenerate saves creature from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        // Set up combat: player2's 5/5 attacks, player1's 2/2 bears (with regen shield) blocks
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        bear.setBlocking(true);
        bear.addBlockingTarget(0);

        Permanent attacker = new Permanent(new com.github.laxika.magicalvibes.cards.s.SerraAngel());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Bears should survive via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        Permanent survivedBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(survivedBear.isTapped()).isTrue();
        assertThat(survivedBear.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Regenerate can target opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regenerate fizzles if target creature is removed")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot cast Regenerate without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Regenerate()));

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Regenerate")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
