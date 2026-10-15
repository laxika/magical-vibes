package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CyclopsGladiator.class, RuneclawBear.class})
class CyclopsGladiatorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues attack trigger for target selection")
    void attackTriggersTargetSelection() {
        addCreatureReady(player1, new CyclopsGladiator());
        addCreatureReady(player2, new RuneclawBear());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Choosing target puts MayEffect trigger on the stack")
    void choosingTargetPutsTriggerOnStack() {
        Permanent cyclops = addCreatureReady(player1, new CyclopsGladiator());
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Cyclops Gladiator")
                        && se.getTargetId().equals(opponentCreature.getId())
                        && se.getSourcePermanentId().equals(cyclops.getId()));
    }

    @Test
    @DisplayName("Resolving trigger presents may ability choice")
    void resolvingTriggerPresentsMayChoice() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new CyclopsGladiator());
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining may ability does not deal any damage")
    void decliningMayDealsNoDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent cyclops = addCreatureReady(player1, new CyclopsGladiator());
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // Neither creature should have taken damage
        assertThat(opponentCreature.getMarkedDamage()).isZero();
        assertThat(cyclops.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Accepting may ability deals mutual damage — Cyclops 4/4 vs 2/2")
    void acceptingDealsMutualDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent cyclops = addCreatureReady(player1, new CyclopsGladiator());
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        // Accepting the choice resolves the damage immediately.
        harness.handleMayAbilityChosen(player1, true);

        // Opponent's 2/2 takes 4 damage from Cyclops (lethal) — should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()));

        assertThat(cyclops.getMarkedDamage()).isEqualTo(2);

        // Cyclops 4/4 takes 2 damage from the 2/2 — should survive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(cyclops.getId()));
    }

    @Test
    @DisplayName("Both creatures die when they deal lethal damage to each other")
    void bothCreaturesDieWhenMutualLethal() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent cyclops = addCreatureReady(player1, new CyclopsGladiator());
        // Put a 4/4 on opponent's side
        Permanent opponentCyclops = addCreatureReady(player2, new CyclopsGladiator());

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

    @Test
    @DisplayName("If Cyclops Gladiator leaves battlefield before resolution, still deals damage to target using last known power")
    void sourceLeavesBattlefieldStillDealsDamageToTarget() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent cyclops = addCreatureReady(player1, new CyclopsGladiator());
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        // Remove Cyclops Gladiator before the triggered ability resolves (simulating opponent's removal spell)
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(cyclops.getId()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Target should still take damage equal to Cyclops Gladiator's last known power (4)
        // Opponent's 2/2 creature takes 4 damage — lethal, should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("If Cyclops Gladiator leaves battlefield, target does not deal reciprocal damage")
    void sourceLeavesBattlefieldNoReciprocalDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent cyclops = addCreatureReady(player1, new CyclopsGladiator());
        // Use a large creature so we can verify no damage is dealt to player
        Permanent opponentCyclops = addCreatureReady(player2, new CyclopsGladiator());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentCyclops.getId());
        // Remove source before resolution
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(cyclops.getId()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // The target takes lethal damage from the departed source.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCyclops.getId()));
        // Player 1 life should be unchanged — no reciprocal damage redirected to player
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target own creatures — only opponent's creatures are valid targets")
    void cannotTargetOwnCreatures() {
        Permanent cyclops = addCreatureReady(player1, new CyclopsGladiator());
        Permanent ownCreature = addCreatureReady(player1, new RuneclawBear());
        Permanent opponentCreature = addCreatureReady(player2, new RuneclawBear());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(opponentCreature.getId())
                .doesNotContain(cyclops.getId(), ownCreature.getId());
    }

    @Test
    @DisplayName("Trigger skipped when opponent has no creatures")
    void triggerSkippedWhenNoValidTargets() {
        addCreatureReady(player1, new CyclopsGladiator());
        // No creatures on opponent's battlefield

        declareAttackers(player1, List.of(0));

        // No target selection should be prompted — trigger auto-skipped
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("A departed target causes the attack ability to do nothing")
    void departedTargetMakesAbilityDoNothing() {
        Permanent cyclops = addCreatureReady(player1, new CyclopsGladiator());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(cyclops.getMarkedDamage()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
