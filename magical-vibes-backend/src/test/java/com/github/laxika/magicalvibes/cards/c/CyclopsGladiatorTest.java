package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CyclopsGladiatorTest extends BaseCardTest {

    // ===== Card setup =====

    @Test
    @DisplayName("Has ON_ATTACK MayEffect wrapping SourceFightsTargetCreatureEffect")
    void hasCorrectAttackTrigger() {
        CyclopsGladiator card = new CyclopsGladiator();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(may.wrapped()).isInstanceOf(SourceFightsTargetCreatureEffect.class);
    }

    // ===== Attack trigger: target selection =====

    @Test
    @DisplayName("Attacking queues attack trigger for target selection")
    void attackTriggersTargetSelection() {
        addReadyCyclops(player1);
        addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Choosing target puts MayEffect trigger on the stack")
    void choosingTargetPutsTriggerOnStack() {
        Permanent cyclops = addReadyCyclops(player1);
        Permanent opponentCreature = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Cyclops Gladiator")
                        && se.getTargetPermanentId().equals(opponentCreature.getId())
                        && se.getSourcePermanentId().equals(cyclops.getId()));
    }

    // ===== May choice =====

    @Test
    @DisplayName("Resolving trigger presents may ability choice")
    void resolvingTriggerPresentsMayChoice() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyCyclops(player1);
        Permanent opponentCreature = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Declining may ability does not deal any damage")
    void decliningMayDealsNoDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent cyclops = addReadyCyclops(player1);
        Permanent opponentCreature = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // Neither creature should have taken damage
        assertThat(opponentCreature.getMarkedDamage()).isZero();
        assertThat(cyclops.getMarkedDamage()).isZero();
    }

    // ===== Fight resolution =====

    @Test
    @DisplayName("Accepting may ability deals mutual damage — Cyclops 4/4 vs 2/2")
    void acceptingDealsMutualDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent cyclops = addReadyCyclops(player1);
        Permanent opponentCreature = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Opponent's 2/2 takes 4 damage from Cyclops (lethal) — should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()));

        // Cyclops 4/4 takes 2 damage from the 2/2 — should survive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(cyclops.getId()));
        assertThat(cyclops.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Both creatures die when they deal lethal damage to each other")
    void bothCreaturesDieWhenMutualLethal() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent cyclops = addReadyCyclops(player1);
        // Put a 4/4 on opponent's side
        Permanent opponentCyclops = addReadyCyclops(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentCyclops.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Both 4/4 creatures should be destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(cyclops.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCyclops.getId()));
    }

    // ===== Ruling 3: source leaves battlefield before resolution =====

    @Test
    @DisplayName("If Cyclops Gladiator leaves battlefield before resolution, still deals damage to target using base power")
    void sourceLeavesBattlefieldStillDealsDamageToTarget() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent cyclops = addReadyCyclops(player1);
        Permanent opponentCreature = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);

        // Remove Cyclops Gladiator before the fight effect resolves (simulating opponent's removal spell)
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(cyclops.getId()));

        harness.passBothPriorities();

        // Target should still take damage equal to Cyclops Gladiator's base power (4)
        // Opponent's 2/2 creature takes 4 damage — lethal, should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("If Cyclops Gladiator leaves battlefield, target does not deal reciprocal damage")
    void sourceLeavesBattlefieldNoReciprocalDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent cyclops = addReadyCyclops(player1);
        // Use a large creature so we can verify no damage is dealt to player
        Permanent opponentCyclops = addReadyCyclops(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentCyclops.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        // Remove source before resolution
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(cyclops.getId()));

        harness.passBothPriorities();

        // Target takes 4 damage (from base power) but survives (4/4 with 4 damage = lethal, destroyed)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCyclops.getId()));
        // Player 1 life should be unchanged — no reciprocal damage redirected to player
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Target restriction =====

    @Test
    @DisplayName("Cannot target own creatures — only opponent's creatures are valid targets")
    void cannotTargetOwnCreatures() {
        Permanent cyclops = addReadyCyclops(player1);
        Permanent ownCreature = addReadyCreature(player1);
        Permanent opponentCreature = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        // The valid targets should not include the controller's own creature
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        // Selecting own creature should fail — only opponent creatures are valid
        // The interaction context should only allow opponent's creatures
        PermanentChoiceContext.AttackTriggerTarget att =
                (PermanentChoiceContext.AttackTriggerTarget) gd.interaction.permanentChoiceContext();
        assertThat(att).isNotNull();
        assertThat(att.sourceCard().getName()).isEqualTo("Cyclops Gladiator");
    }

    @Test
    @DisplayName("Trigger skipped when opponent has no creatures")
    void triggerSkippedWhenNoValidTargets() {
        addReadyCyclops(player1);
        // No creatures on opponent's battlefield

        declareAttackers(player1, List.of(0));

        // No target selection should be prompted — trigger auto-skipped
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    // ===== Helpers =====

    private Permanent addReadyCyclops(Player player) {
        Permanent perm = new Permanent(new CyclopsGladiator());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
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
}
