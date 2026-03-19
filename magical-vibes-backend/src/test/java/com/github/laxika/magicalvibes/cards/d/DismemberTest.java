package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DismemberTest extends BaseCardTest {

    @Test
    @DisplayName("Dismember has correct card properties")
    void hasCorrectCardProperties() {
        Dismember card = new Dismember();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(BoostTargetCreatureEffect.class);

        BoostTargetCreatureEffect effect = (BoostTargetCreatureEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(-5);
        assertThat(effect.toughnessBoost()).isEqualTo(-5);
    }

    @Test
    @DisplayName("Casting Dismember puts it on stack with target creature")
    void castingPutsItOnStack() {
        harness.addToBattlefield(player1, new MahamotiDjinn());
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Mahamoti Djinn");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Dismember");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Dismember gives -5/-5 to target creature")
    void resolvesAndDebuffsTarget() {
        harness.addToBattlefield(player1, new MahamotiDjinn());
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Mahamoti Djinn");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent djinn = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(djinn.getPowerModifier()).isEqualTo(-5);
        assertThat(djinn.getToughnessModifier()).isEqualTo(-5);
        assertThat(djinn.getEffectivePower()).isEqualTo(0);
        assertThat(djinn.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Dismember kills a creature with toughness 5 or less")
    void killsSmallCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Debuff from Dismember wears off at cleanup step")
    void debuffWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new MahamotiDjinn());
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Mahamoti Djinn");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent djinn = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(djinn.getPowerModifier()).isEqualTo(0);
        assertThat(djinn.getToughnessModifier()).isEqualTo(0);
        assertThat(djinn.getEffectivePower()).isEqualTo(5);
        assertThat(djinn.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Dismember fizzles if target is removed")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Dismember")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
