package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiminishTest extends BaseCardTest {

    @Test
    @DisplayName("Diminish has correct card properties")
    void hasCorrectProperties() {
        Diminish card = new Diminish();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(SetBasePowerToughnessUntilEndOfTurnEffect.class);
    }

    @Test
    @DisplayName("Casting Diminish puts it on the stack with target creature")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Diminish");
        assertThat(entry.getTargetPermanentId()).isEqualTo(bearId);
    }

    @Test
    @DisplayName("Resolving Diminish sets target creature's base power and toughness to 1/1")
    void setsBasePowerToughnessToOneOne() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        // Grizzly Bears is normally 2/2 — after Diminish it should be 1/1
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.isBasePowerToughnessOverriddenUntilEndOfTurn()).isTrue();
        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Diminish allows modifiers to apply on top of new base P/T")
    void modifiersApplyOnTopOfNewBase() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        // Give the bear +2/+2 boost
        bear.setPowerModifier(2);
        bear.setToughnessModifier(2);
        assertThat(bear.getEffectivePower()).isEqualTo(4); // 2 base + 2 modifier
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);

        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        // Base becomes 1/1, modifiers still apply: 1 + 2 = 3
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Diminish allows +1/+1 counters to apply on top of new base P/T")
    void countersApplyOnTopOfNewBase() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        bear.setPlusOnePlusOneCounters(2);
        assertThat(bear.getEffectivePower()).isEqualTo(4); // 2 base + 2 counters
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);

        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        // Base becomes 1/1, counters still apply: 1 + 2 = 3
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Diminish wears off at cleanup step")
    void wearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(1);

        // Move to cleanup
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // After cleanup, base P/T override is removed
        assertThat(bear.isBasePowerToughnessOverriddenUntilEndOfTurn()).isFalse();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Diminish can target opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Diminish")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Diminish fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Diminish overrides animated land's base P/T (layer 7b timestamp)")
    void overridesAnimatedLandBasePowerToughness() {
        // Animate Treetop Village into a 3/3 creature, then Diminish it
        Permanent village = addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Village is now a 3/3 animated creature
        assertThat(village.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(village.getEffectivePower()).isEqualTo(3);
        assertThat(village.getEffectiveToughness()).isEqualTo(3);

        // Cast Diminish on the animated village
        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, village.getId());
        harness.passBothPriorities();

        // Per layer 7b: Diminish has later timestamp, overrides animation's 3/3 → 1/1
        assertThat(village.getEffectivePower()).isEqualTo(1);
        assertThat(village.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Diminish overrides permanently animated permanent's base P/T")
    void overridesPermanentlyAnimatedBasePowerToughness() {
        // Simulate a permanently animated artifact (e.g. Tezzeret's -1 making it a 5/5)
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent perm = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        perm.setPermanentlyAnimated(true);
        perm.setPermanentAnimatedPower(5);
        perm.setPermanentAnimatedToughness(5);

        assertThat(perm.getEffectivePower()).isEqualTo(5);
        assertThat(perm.getEffectiveToughness()).isEqualTo(5);

        // Cast Diminish
        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, perm.getId());
        harness.passBothPriorities();

        // Diminish overrides the permanently animated 5/5 → 1/1
        assertThat(perm.getEffectivePower()).isEqualTo(1);
        assertThat(perm.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Animated land reverts to animated P/T after Diminish wears off at cleanup")
    void animatedLandRevertsAfterDiminishWearsOff() {
        // Both animation and Diminish are "until end of turn", so both wear off at cleanup
        Permanent village = addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, village.getId());
        harness.passBothPriorities();

        assertThat(village.getEffectivePower()).isEqualTo(1);

        // Move to cleanup — both effects wear off
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Village is no longer a creature, no longer has base P/T override
        assertThat(village.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(village.isBasePowerToughnessOverriddenUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, village)).isFalse();
    }

    @Test
    @DisplayName("Diminish goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Diminish"));
    }

    private Permanent addVillageReady(Player player) {
        TreetopVillage card = new TreetopVillage();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
