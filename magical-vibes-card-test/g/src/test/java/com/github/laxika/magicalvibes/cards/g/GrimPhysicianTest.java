package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrimPhysician.class, NyxbornCourser.class})
class GrimPhysicianTest extends BaseCardTest {

    @Test
    @DisplayName("When Grim Physician dies, it targets an opponent's creature")
    void deathTriggerTargetsOpponentsCreature() {
        harness.addToBattlefield(player1, new GrimPhysician());
        harness.addToBattlefield(player2, new NyxbornCourser());
        UUID targetId = harness.getPermanentId(player2, "Nyxborn Courser");
        setupCombatWherePhysicianDies();

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(targetId);

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Nyxborn Courser");
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("When Grim Physician dies, its trigger cannot target a creature you control")
    void deathTriggerCannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrimPhysician());
        harness.addToBattlefield(player1, new NyxbornCourser());
        UUID ownCreatureId = harness.getPermanentId(player1, "Nyxborn Courser");
        setupCombatWherePhysicianDies();

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(ownCreatureId);
    }

    @Test
    @DisplayName("The death trigger's reduction wears off at end of turn")
    void reductionWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrimPhysician());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        setupCombatWherePhysicianDies();
        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The death trigger puts a creature with zero toughness into the graveyard")
    void reductionKillsOneToughnessCreature() {
        harness.addToBattlefield(player1, new GrimPhysician());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrimPhysician());
        setupCombatWherePhysicianDies();
        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grim Physician");
        harness.assertInGraveyard(player2, "Grim Physician");
    }

    @Test
    @DisplayName("The death trigger has no legal target when only your creatures remain")
    void noOpponentCreatureMeansNoTarget() {
        Permanent physician = harness.addToBattlefieldAndReturn(player1, new GrimPhysician());
        harness.addToBattlefield(player1, new NyxbornCourser());
        physician.setMarkedDamage(1);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grim Physician");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        Permanent ownCreature = findPermanent(player1, "Nyxborn Courser");
        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
    }

    private void setupCombatWherePhysicianDies() {
        Permanent physician = findPermanent(player1, "Grim Physician");
        physician.setSummoningSick(false);
        physician.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new NyxbornCourser());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
    }
}
