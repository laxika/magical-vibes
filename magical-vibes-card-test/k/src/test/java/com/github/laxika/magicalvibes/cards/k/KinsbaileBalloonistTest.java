package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KinsbaileBalloonist.class, HillcomberGiant.class})
class KinsbaileBalloonistTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues target selection for a creature")
    void attackTriggerQueuesForTargetSelection() {
        addCreatureReady(player1, new KinsbaileBalloonist());
        addCreatureReady(player1, new HillcomberGiant());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Accepting the may grants flying to the targeted creature until end of turn")
    void acceptingMayGrantsFlying() {
        addCreatureReady(player1, new KinsbaileBalloonist());
        Permanent giant = addCreatureReady(player1, new HillcomberGiant());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(giant.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining the may leaves the targeted creature without flying")
    void decliningMayLeavesNoFlying() {
        addCreatureReady(player1, new KinsbaileBalloonist());
        Permanent giant = addCreatureReady(player1, new HillcomberGiant());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(giant.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Accepting the may can grant flying to an opponent's creature")
    void acceptingMayGrantsFlyingToOpponentsCreature() {
        addCreatureReady(player1, new KinsbaileBalloonist());
        Permanent giant = addCreatureReady(player2, new HillcomberGiant());

        assertThat(giant.hasKeyword(Keyword.FLYING)).isFalse();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(giant.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new KinsbaileBalloonist());
        Permanent giant = addCreatureReady(player1, new HillcomberGiant());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(giant.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(giant.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger can target the Balloonist itself")
    void canTargetItself() {
        Permanent balloonist = addCreatureReady(player1, new KinsbaileBalloonist());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, balloonist.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(balloonist.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(balloonist.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger resolves after the Balloonist leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent balloonist = addCreatureReady(player1, new KinsbaileBalloonist());
        Permanent giant = addCreatureReady(player1, new HillcomberGiant());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, balloonist));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(giant.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
