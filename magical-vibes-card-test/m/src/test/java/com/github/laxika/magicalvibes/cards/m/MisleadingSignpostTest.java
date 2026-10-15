package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.t.TheAetherspark;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MisleadingSignpost.class, LlanowarElves.class, JaceBeleren.class,
        InvasionOfZendikar.class, TheAetherspark.class})
class MisleadingSignpostTest extends BaseCardTest {

    @Test
    void addsBlueMana() {
        Permanent signpost = harness.addToBattlefieldAndReturn(player1, new MisleadingSignpost());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(signpost), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(signpost.isTapped()).isTrue();
    }

    @Test
    void reselectsAnAttackingCreatureTargetDuringDeclareAttackers() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        attacker.setAttacking(true);
        attacker.setAttackTarget(planeswalker.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MisleadingSignpost()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        harness.castArtifact(player1, 0);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenTargetTrigger.class);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isEqualTo(new PermanentChoiceContext.ReselectAttackingCreatureTarget(attacker.getId()));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handlePermanentChosen(player1, player1.getId()));

        assertThat(attacker.getAttackTarget()).isEqualTo(player1.getId());
    }

    @Test
    void doesNotTriggerOutsideDeclareAttackers() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.enterBattlefieldAndReturn(player1, new MisleadingSignpost());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(attacker.getAttackTarget()).isEqualTo(player1.getId());
    }

    @Test
    void mayDeclineReselection() {
        Permanent attacker = prepareAttacker();
        harness.enterBattlefieldAndReturn(player1, new MisleadingSignpost());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMayAbilityChosen(player1, false));

        assertThat(attacker.getAttackTarget()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canRedirectToOpposingPlaneswalkerButNotAttackersControllerOrTheirPlaneswalker() {
        Permanent attacker = prepareAttacker();
        Permanent opposingJace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        Permanent ownJace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        opposingJace.setCounterCount(CounterType.LOYALTY, 3);
        ownJace.setCounterCount(CounterType.LOYALTY, 3);
        beginReselection(attacker);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(player1.getId(), opposingJace.getId())
                .doesNotContain(player2.getId(), ownJace.getId(), attacker.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handlePermanentChosen(player1, opposingJace.getId()));

        assertThat(attacker.getAttackTarget()).isEqualTo(opposingJace.getId());
        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    void canRedirectToBattleControlledByAttackerControllerAndProtectedByOpponent() {
        Permanent attacker = prepareAttacker();
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player1.getId());
        beginReselection(attacker);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(battle.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handlePermanentChosen(player1, battle.getId()));

        assertThat(attacker.getAttackTarget()).isEqualTo(battle.getId());
    }

    @Test
    void reselectionIgnoresRestrictionOnAttackingAttachedAetherspark() {
        Permanent attacker = prepareAttacker();
        Permanent equippedCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent aetherspark = harness.addToBattlefieldAndReturn(player1, new TheAetherspark());
        aetherspark.setCounterCount(CounterType.LOYALTY, 4);
        aetherspark.setAttachedTo(equippedCreature.getId());
        beginReselection(attacker);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(aetherspark.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handlePermanentChosen(player1, aetherspark.getId()));

        assertThat(attacker.getAttackTarget()).isEqualTo(aetherspark.getId());
    }

    @Test
    void doesNotReselectCreatureThatStoppedAttackingBeforeResolution() {
        Permanent attacker = prepareAttacker();
        harness.enterBattlefieldAndReturn(player1, new MisleadingSignpost());
        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setAttacking(false);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.inMutationScope(
                        () -> harness.getStackResolutionService().resolveTopOfStack(gd)));

        assertThat(attacker.getAttackTarget()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent prepareAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        return attacker;
    }

    private void beginReselection(Permanent attacker) {
        harness.enterBattlefieldAndReturn(player1, new MisleadingSignpost());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);
    }
}
