package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajicLegionsEdge.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class TajicLegionsEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows Tajic to attack while summoning sick")
    void hasteAllowsImmediateAttack() {
        Permanent tajic = harness.addToBattlefieldAndReturn(player1, new TajicLegionsEdge());
        tajic.setSummoningSick(true);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(tajic.isAttacking()).isTrue();
        assertThat(tajic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mentor targets only an attacking creature with lesser power")
    void mentorTargetsAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new TajicLegionsEdge());
        Permanent attackingWizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent nonAttackingWizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent equalPowerCreature = addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0, 1, 3));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attackingWizard.getId());

        harness.handlePermanentChosen(player1, attackingWizard.getId());
        resolveAllTriggers();

        assertThat(attackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(equalPowerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Prevents noncombat damage to another creature you control")
    void preventsNoncombatDamageToAnotherCreatureYouControl() {
        addCreatureReady(player1, new TajicLegionsEdge());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent noncombat damage to Tajic")
    void doesNotPreventNoncombatDamageToTajic() {
        Permanent tajic = harness.addToBattlefieldAndReturn(player1, new TajicLegionsEdge());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, tajic.getId());

        assertThat(tajic.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability grants first strike until end of turn")
    void activatedAbilityGrantsFirstStrikeUntilEndOfTurn() {
        Permanent tajic = addCreatureReady(player1, new TajicLegionsEdge());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, tajic, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, tajic, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not prevent noncombat damage to opposing creatures")
    void doesNotProtectOpposingCreatures() {
        harness.addToBattlefield(player1, new TajicLegionsEdge());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not prevent damage to its controller")
    void doesNotProtectController() {
        harness.addToBattlefield(player1, new TajicLegionsEdge());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Protection ends when Tajic dies")
    void protectionEndsWhenTajicDies() {
        Permanent tajic = harness.addToBattlefieldAndReturn(player1, new TajicLegionsEdge());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, tajic.getId());
        harness.assertInGraveyard(player1, "Tajic, Legion's Edge");
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not prevent combat damage to other creatures")
    void doesNotPreventCombatDamage() {
        addCreatureReady(player1, new TajicLegionsEdge());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Tajic, Legion's Edge");
    }

    @Test
    @DisplayName("Mentor rechecks lesser power when it resolves")
    void mentorRechecksPowerOnResolution() {
        addCreatureReady(player1, new TajicLegionsEdge());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, bears.getId());
            bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
            resolveAllTriggers();
        });

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mentor resolves using Tajic's last known power after it dies")
    void mentorResolvesAfterTajicDies() {
        Permanent tajic = addCreatureReady(player1, new TajicLegionsEdge());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, bears.getId());
            harness.castAndResolveInstant(player1, 0, tajic.getId());
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player1, "Tajic, Legion's Edge");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
