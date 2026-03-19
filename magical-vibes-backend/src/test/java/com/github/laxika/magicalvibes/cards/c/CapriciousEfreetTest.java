package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CapriciousEfreetTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Capricious Efreet has upkeep triggered destroy-one-at-random ability")
    void hasCorrectAbilityStructure() {
        CapriciousEfreet card = new CapriciousEfreet();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(DestroyOneOfTargetsAtRandomEffect.class);
    }

    // ===== Trigger: own target selection (step 1) =====

    @Test
    @DisplayName("Upkeep trigger presents own nonland permanent selection")
    void upkeepTriggerPresentsOwnTargetSelection() {
        addReadyEfreet(player1);
        addReadyCreature(player1, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Efreet can target itself as own nonland permanent")
    void efreetCanTargetItself() {
        Permanent efreet = addReadyEfreet(player1);
        // No other nonland permanents — only the Efreet itself is a valid own target

        advanceToUpkeep(player1);

        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose the Efreet itself as own target
        harness.handlePermanentChosen(player1, efreet.getId());

        // No opponent nonland permanents, so ability goes directly to stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds()).containsExactly(efreet.getId());
    }

    // ===== Trigger: opponent target selection (step 2) =====

    @Test
    @DisplayName("After own target, presents opponent nonland permanent selection")
    void afterOwnTargetPresentsOpponentSelection() {
        addReadyEfreet(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player2, new HillGiant());

        advanceToUpkeep(player1);

        // Step 1: choose own target
        harness.handlePermanentChosen(player1, bears.getId());

        // Step 2: multi-permanent choice for opponent targets
        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Can choose zero opponent targets (skipping optional targets)")
    void canChooseZeroOpponentTargets() {
        addReadyEfreet(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player2, new HillGiant());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of()); // zero opponents

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactly(bears.getId());
    }

    @Test
    @DisplayName("Can choose one opponent target")
    void canChooseOneOpponentTarget() {
        addReadyEfreet(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        Permanent hillGiant = addReadyCreature(player2, new HillGiant());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(hillGiant.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactly(bears.getId(), hillGiant.getId());
    }

    @Test
    @DisplayName("Can choose two opponent targets")
    void canChooseTwoOpponentTargets() {
        addReadyEfreet(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        Permanent hillGiant = addReadyCreature(player2, new HillGiant());
        Permanent bears2 = addReadyCreature(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(hillGiant.getId(), bears2.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactly(bears.getId(), hillGiant.getId(), bears2.getId());
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving with only own target destroys it")
    void resolvingWithOnlyOwnTargetDestroysIt() {
        Permanent efreet = addReadyEfreet(player1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, efreet.getId());

        // No opponent targets → stack entry with just own target
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve

        // Efreet destroyed itself (only target, random pick with 1 element)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(efreet.getId()));
    }

    @Test
    @DisplayName("Resolving destroys exactly one permanent from the target pool")
    void resolvingDestroysExactlyOnePermanent() {
        addReadyEfreet(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        Permanent hillGiant = addReadyCreature(player2, new HillGiant());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(hillGiant.getId()));

        int totalBefore = gd.playerBattlefields.get(player1.getId()).size()
                + gd.playerBattlefields.get(player2.getId()).size();

        harness.passBothPriorities(); // resolve

        int totalAfter = gd.playerBattlefields.get(player1.getId()).size()
                + gd.playerBattlefields.get(player2.getId()).size();

        // Exactly one permanent should have been destroyed
        assertThat(totalBefore - totalAfter).isEqualTo(1);
    }

    @Test
    @DisplayName("If all targets leave before resolution, ability fizzles")
    void abilityFizzlesIfAllTargetsLeave() {
        addReadyEfreet(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).remove(bears);

        int battlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.passBothPriorities(); // resolve — target gone

        // Nothing should be destroyed
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
    }

    // ===== Targeting constraints =====

    @Test
    @DisplayName("Lands are not valid targets for own permanent selection")
    void landsNotValidOwnTargets() {
        Permanent efreet = addReadyEfreet(player1);
        addReadyLand(player1);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // The only valid own target should be the Efreet itself (not the land)
        // Choosing the Efreet should work
        harness.handlePermanentChosen(player1, efreet.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Skips opponent target step when opponent has no nonland permanents")
    void skipsOpponentStepWhenNoOpponentNonlands() {
        Permanent efreet = addReadyEfreet(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        addReadyLand(player2); // only a land — no valid opponent targets

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());

        // Should go directly to stack (no multi-permanent choice)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactly(bears.getId());
    }

    @Test
    @DisplayName("Stack entry is a triggered ability")
    void stackEntryIsTriggeredAbility() {
        addReadyEfreet(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType())
                .isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    // ===== Helper methods =====

    private Permanent addReadyEfreet(Player player) {
        CapriciousEfreet card = new CapriciousEfreet();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyLand(Player player) {
        Card land = new Card();
        land.setType(CardType.LAND);
        Permanent perm = new Permanent(land);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance UNTAP → UPKEEP, triggers fire
    }
}
