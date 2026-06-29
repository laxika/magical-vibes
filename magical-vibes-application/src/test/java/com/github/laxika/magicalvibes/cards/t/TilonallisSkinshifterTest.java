package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TilonallisSkinshifterTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ATTACK trigger with BecomeCopyOfTargetCreatureUntilEndOfTurnEffect")
    void hasCorrectAbilityStructure() {
        TilonallisSkinshifter card = new TilonallisSkinshifter();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(BecomeCopyOfTargetCreatureUntilEndOfTurnEffect.class);
    }

    @Test
    @DisplayName("Has target filter for another nonlegendary attacking creature")
    void hasCorrectTargetFilter() {
        TilonallisSkinshifter card = new TilonallisSkinshifter();

        assertThat(card.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
    }

    // ===== Attack trigger: target selection =====

    @Test
    @DisplayName("Attacking alongside another creature queues target selection")
    void attackTriggersTargetSelection() {
        addReadySkinshifter(player1);
        addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Choosing target puts copy trigger on the stack")
    void choosingTargetPutsTriggerOnStack() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Tilonalli's Skinshifter")
                        && se.getTargetId().equals(bears.getId())
                        && se.getSourcePermanentId().equals(skinshifter.getId()));
    }

    // ===== Copy resolution =====

    @Test
    @DisplayName("Resolving trigger makes Skinshifter a copy of target creature")
    void becomeCopyOnResolution() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(skinshifter.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(skinshifter.getCard().getPower()).isEqualTo(2);
        assertThat(skinshifter.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Copy gains target creature's abilities")
    void copyGainsTargetAbilities() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        // Should have the target's keywords (Grizzly Bears is vanilla, so no keywords)
        assertThat(skinshifter.getCard().getKeywords()).isEqualTo(bears.getCard().getKeywords());
    }

    // ===== Until end of turn revert =====

    @Test
    @DisplayName("Copy reverts at end of turn")
    void copyRevertsAtEndOfTurn() {
        Permanent skinshifter = addReadySkinshifter(player1);
        addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(skinshifter.getCard().getName()).isEqualTo("Grizzly Bears");

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skinshifter.getCard().getName()).isEqualTo("Tilonalli's Skinshifter");
        assertThat(skinshifter.getCard().getPower()).isEqualTo(0);
        assertThat(skinshifter.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Skinshifter retains its ON_ATTACK ability after revert")
    void retainsAbilityAfterRevert() {
        Permanent skinshifter = addReadySkinshifter(player1);
        addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skinshifter.getCard().getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(skinshifter.getCard().getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(BecomeCopyOfTargetCreatureUntilEndOfTurnEffect.class);
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Trigger does not fire when attacking alone (no other attacking creatures)")
    void triggerDoesNotFireWhenAttackingAlone() {
        addReadySkinshifter(player1);

        declareAttackers(player1, List.of(0));

        // No valid targets - trigger should not queue for target selection
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addReadySkinshifter(player1);
        addReadyCreature(player1, new GrizzlyBears()); // non-attacking
        addReadyCreature(player2, new GrizzlyBears()); // on opponent's side, also non-attacking

        // Only Skinshifter attacks (index 0), Bears at index 1 does not attack
        declareAttackers(player1, List.of(0));

        // No valid targets since no other creature is attacking
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Cannot target legendary attacking creature")
    void cannotTargetLegendaryCreature() {
        addReadySkinshifter(player1);
        addReadyCreature(player1, new ThrunTheLastTroll());

        declareAttackers(player1, List.of(0, 1));

        // Thrun is legendary — no valid targets
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Copy fizzles if target creature is removed before resolution")
    void copyFizzlesIfTargetRemoved() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).remove(bears);

        resolveAllTriggers();

        // Skinshifter should remain unchanged
        assertThat(skinshifter.getCard().getName()).isEqualTo("Tilonalli's Skinshifter");
    }

    @Test
    @DisplayName("Skinshifter remains on battlefield after copy resolves")
    void skinshifterRemainsOnBattlefield() {
        Permanent skinshifter = addReadySkinshifter(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skinshifter);
    }

    @Test
    @DisplayName("Stack is empty after copy trigger resolves")
    void stackEmptyAfterResolution() {
        addReadySkinshifter(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addReadySkinshifter(Player player) {
        Permanent perm = new Permanent(new TilonallisSkinshifter());
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

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
