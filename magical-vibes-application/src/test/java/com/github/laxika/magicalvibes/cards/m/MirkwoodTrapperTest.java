package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirkwoodTrapper.class, GrizzlyBears.class})
class MirkwoodTrapperTest extends BaseCardTest {

    @Test
    void controllerChoosesAnAttackingCreatureToDebuffWhenAttacked() {
        addCreatureReady(player1, new MirkwoodTrapper());
        Permanent firstAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstAttacker.getId(), secondAttacker.getId());

        harness.handlePermanentChosen(player1, firstAttacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isZero();
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(2);
    }

    @Test
    void playerAttackingSomewhereElseChoosesAnAttackingCreatureToBoost() {
        addCreatureReady(player1, new MirkwoodTrapper());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackingPlayerChoosesCreatureToBoost.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstAttacker.getId(), secondAttacker.getId());

        harness.handlePermanentChosen(player1, secondAttacker.getId());

        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(4);
    }

    @Test
    void singleAttackerBoostExpiresAtCleanup() {
        Permanent trapper = addCreatureReady(player1, new MirkwoodTrapper());

        declareAttackers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gqs.getEffectivePower(gd, trapper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, trapper)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, trapper)).isEqualTo(1);
    }

    @Test
    void debuffExpiresAtCleanup() {
        addCreatureReady(player1, new MirkwoodTrapper());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gqs.getEffectivePower(gd, attacker)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    void debuffDoesNotResolveIfTargetStopsAttacking() {
        addCreatureReady(player1, new MirkwoodTrapper());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, attacker.getId()));
        attacker.setAttacking(false);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    void boostChoosesOnlyCreaturesStillAttackingAtResolution() {
        Permanent trapper = addCreatureReady(player1, new MirkwoodTrapper());
        Permanent remainingAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent removedAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));
        removedAttacker.setAttacking(false);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gqs.getEffectivePower(gd, remainingAttacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, removedAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, trapper)).isEqualTo(1);
    }
}
