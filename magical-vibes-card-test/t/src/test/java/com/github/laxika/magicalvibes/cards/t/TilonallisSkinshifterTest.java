package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.c.CrashTheRamparts;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.k.KopalaWardenOfWaves;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.m.MerfolkBranchwalker;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TilonallisSkinshifter.class, QueensBaySoldier.class, CrashTheRamparts.class,
        KopalaWardenOfWaves.class, ColossalDreadmaw.class, MerfolkBranchwalker.class, TilonallisKnight.class})
class TilonallisSkinshifterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alongside another creature queues target selection")
    void attackTriggersTargetSelection() {
        addReadySkinshifter(player1);
        addCreatureReady(player1, new QueensBaySoldier());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Choosing target puts copy trigger on the stack")
    void choosingTargetPutsTriggerOnStack() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent target = addCreatureReady(player1, new QueensBaySoldier());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Tilonalli's Skinshifter")
                        && se.getTargetId().equals(target.getId())
                        && se.getSourcePermanentId().equals(skinshifter.getId()));
    }

    @Test
    @DisplayName("Resolving trigger makes Skinshifter a copy of target creature")
    void becomeCopyOnResolution() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent target = addCreatureReady(player1, new QueensBaySoldier());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(skinshifter.getCard().getName()).isEqualTo("Queen's Bay Soldier");
        assertThat(skinshifter.getCard().getPower()).isEqualTo(2);
        assertThat(skinshifter.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Copy gains target creature's abilities")
    void copyGainsTargetAbilities() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent target = addCreatureReady(player1, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A pump resolved before becoming a copy continues to apply (CR 611.2c)")
    void pumpBeforeCopyPersistsOnTheCopy() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent target = addCreatureReady(player1, new QueensBaySoldier());

        // Crash the Ramparts resolves on the Skinshifter before it becomes a copy.
        harness.setHand(player1, List.of(new CrashTheRamparts()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, skinshifter.getId());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        // The resolved pump continues to apply after the copy effect.
        assertThat(skinshifter.getCard().getName()).isEqualTo("Queen's Bay Soldier");
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Copy reverts at end of turn")
    void copyRevertsAtEndOfTurn() {
        Permanent skinshifter = addReadySkinshifter(player1);
        addCreatureReady(player1, new QueensBaySoldier());

        declareAttackers(player1, List.of(0, 1));
        Permanent target = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(skinshifter.getCard().getName()).isEqualTo("Queen's Bay Soldier");

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skinshifter.getCard().getName()).isEqualTo("Tilonalli's Skinshifter");
        assertThat(skinshifter.getCard().getPower()).isEqualTo(0);
        assertThat(skinshifter.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Skinshifter can copy another attacker again after reverting")
    void retainsAbilityAfterRevert() {
        Permanent skinshifter = addReadySkinshifter(player1);
        addCreatureReady(player1, new QueensBaySoldier());

        declareAttackers(player1, List.of(0, 1));
        Permanent target = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(skinshifter.getCard().getName()).isEqualTo("Queen's Bay Soldier");
    }

    @Test
    @DisplayName("Trigger does not fire when attacking alone (no other attacking creatures)")
    void triggerDoesNotFireWhenAttackingAlone() {
        addReadySkinshifter(player1);

        declareAttackers(player1, List.of(0));

        // No valid targets - trigger should not queue for target selection
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addReadySkinshifter(player1);
        addCreatureReady(player1, new QueensBaySoldier()); // non-attacking
        addCreatureReady(player2, new QueensBaySoldier()); // on opponent's side, also non-attacking

        // Only Skinshifter attacks (index 0), Soldier at index 1 does not attack
        declareAttackers(player1, List.of(0));

        // No valid targets since no other creature is attacking
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Cannot target legendary attacking creature")
    void cannotTargetLegendaryCreature() {
        addReadySkinshifter(player1);
        addCreatureReady(player1, new KopalaWardenOfWaves());

        declareAttackers(player1, List.of(0, 1));

        // Kopala is legendary, so there is no legal target.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Copy fizzles if target creature is removed before resolution")
    void copyFizzlesIfTargetRemoved() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent target = addCreatureReady(player1, new QueensBaySoldier());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).remove(target);

        resolveAllTriggers();

        // Skinshifter should remain unchanged
        assertThat(skinshifter.getCard().getName()).isEqualTo("Tilonalli's Skinshifter");
    }

    @Test
    @DisplayName("Skinshifter remains on battlefield after copy resolves")
    void skinshifterRemainsOnBattlefield() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent target = addCreatureReady(player1, new QueensBaySoldier());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skinshifter);
    }

    @Test
    @DisplayName("Stack is empty after copy trigger resolves")
    void stackEmptyAfterResolution() {
        addReadySkinshifter(player1);
        Permanent target = addCreatureReady(player1, new QueensBaySoldier());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Copy does not inherit counters or a pump on the target")
    void doesNotCopyTargetModifiers() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent soldier = addCreatureReady(player1, new QueensBaySoldier());
        soldier.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new CrashTheRamparts()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, soldier.getId());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, soldier.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Skinshifter keeps its own counters when it copies")
    void retainsOwnCounters() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent soldier = addCreatureReady(player1, new QueensBaySoldier());
        skinshifter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, soldier.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Target must still be attacking when the copy trigger resolves")
    void targetRemovedFromCombatIsIllegal() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent soldier = addCreatureReady(player1, new QueensBaySoldier());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, soldier.getId());
        soldier.setAttacking(false);
        resolveAllTriggers();

        assertThat(skinshifter.getCard().getName()).isEqualTo("Tilonalli's Skinshifter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Copying an enters ability does not trigger it")
    void copyingDoesNotTriggerEnterAbility() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent branchwalker = addCreatureReady(player1, new MerfolkBranchwalker());
        harness.setLibrary(player1, List.of(new QueensBaySoldier()));

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, branchwalker.getId());
        resolveAllTriggers();

        assertThat(skinshifter.getCard().getName()).isEqualTo("Merfolk Branchwalker");
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Copy persists through the end step after its target leaves")
    void copyPersistsAfterTargetLeaves() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent soldier = addCreatureReady(player1, new QueensBaySoldier());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, soldier.getId());
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(soldier);
        harness.forceStep(TurnStep.END_STEP);

        assertThat(skinshifter.getCard().getName()).isEqualTo("Queen's Bay Soldier");
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(2);
        skinshifter.setMarkedDamage(1);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skinshifter);
        assertThat(skinshifter.getCard().getName()).isEqualTo("Tilonalli's Skinshifter");
        assertThat(skinshifter.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Copied attack abilities do not trigger retroactively")
    void copiedAttackAbilityDoesNotTrigger() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent knight = addCreatureReady(player1, new TilonallisKnight());
        addCreatureReady(player1, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, knight.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, skinshifter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skinshifter)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySkinshifter(Player player) {
        return addCreatureReady(player, new TilonallisSkinshifter());
    }
}
