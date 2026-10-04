package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FirefistStriker.class, GrizzlyBears.class})
class FirefistStrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion stops the targeted creature from blocking")
    void battalionStopsTargetFromBlocking() {
        addCreatureReady(player1, new FirefistStriker());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, opposing.getId());
        resolveAllTriggers();

        assertThat(opposing.isCantBlockThisTurn()).isTrue();

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Battalion does not trigger with only one other attacker")
    void noTriggerWithTooFewAttackers() {
        addCreatureReady(player1, new FirefistStriker());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(opposing.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Battalion requires Firefist Striker itself to attack")
    void noTriggerWhenStrikerDoesNotAttack() {
        addCreatureReady(player1, new FirefistStriker());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2, 3));

        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(opposing.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Battalion can target a creature controlled by its controller")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new FirefistStriker());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        harness.handlePermanentChosen(player1, ownCreature.getId());
        resolveAllTriggers();

        assertThat(ownCreature.isCantBlockThisTurn()).isTrue();
        assertThat(opposing.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Battalion still resolves after the attackers leave combat")
    void resolvesAfterAttackersLeaveCombat() {
        Permanent striker = addCreatureReady(player1, new FirefistStriker());
        Permanent firstOther = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondOther = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        harness.handlePermanentChosen(player1, opposing.getId());
        striker.setAttacking(false);
        firstOther.setAttacking(false);
        secondOther.setAttacking(false);
        resolveAllTriggers();

        assertThat(opposing.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Battalion's blocking restriction expires at end of turn")
    void blockingRestrictionExpires() {
        addCreatureReady(player1, new FirefistStriker());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        harness.handlePermanentChosen(player1, opposing.getId());
        resolveAllTriggers();
        assertThat(opposing.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(opposing.isCantBlockThisTurn()).isFalse();
    }
}
