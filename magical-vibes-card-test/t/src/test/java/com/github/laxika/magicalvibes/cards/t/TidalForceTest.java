package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.w.WallOfDenial;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TidalForce.class, SolRing.class, WallOfDenial.class})
class TidalForceTest extends BaseCardTest {

    @Test
    void controllerMayTapTargetPermanentDuringAnyUpkeep() {
        harness.addToBattlefield(player1, new TidalForce());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SolRing());

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void controllerMayUntapTargetPermanentDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new TidalForce());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        target.tap();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void decliningMayAbilityLeavesTargetUnchanged() {
        harness.addToBattlefield(player1, new TidalForce());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SolRing());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void shroudedPermanentIsNotAValidTarget() {
        harness.addToBattlefield(player1, new TidalForce());
        Permanent shroudedTarget = harness.addToBattlefieldAndReturn(player1, new WallOfDenial());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(shroudedTarget.getId());
    }

    @Test
    void targetIsChosenBeforePlayersCanRespondAndMayChoiceWaitsForResolution() {
        harness.addToBattlefield(player1, new TidalForce());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canTargetItselfAndUntapDuringOpponentsUpkeep() {
        Permanent force = harness.addToBattlefieldAndReturn(player1, new TidalForce());
        force.tap();

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player1, force.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(force.isTapped()).isFalse();
    }
}
