package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HighRiseSawjack.class, GrizzlyBears.class, SuntailHawk.class})
class HighRiseSawjackTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature with flying triggers +2/+0")
    void blockingFlyingCreatureTriggersBoost() {
        Permanent sawjack = addCreatureReady(player2, new HighRiseSawjack());
        addReadyAttacker(player1, new SuntailHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, sawjack)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, sawjack)).isZero();
    }

    @Test
    @DisplayName("Blocking a creature without flying does not trigger the boost")
    void blockingNonFlyingCreatureDoesNotTriggerBoost() {
        Permanent sawjack = addCreatureReady(player2, new HighRiseSawjack());
        addReadyAttacker(player1, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, sawjack)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, sawjack)).isZero();
    }

    @Test
    @DisplayName("The boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent sawjack = addCreatureReady(player2, new HighRiseSawjack());
        addReadyAttacker(player1, new SuntailHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, sawjack)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, sawjack)).isZero();
    }

    @Test
    @DisplayName("The block trigger resolves after the flying attacker leaves the battlefield")
    void boostResolvesAfterAttackerLeaves() {
        Permanent sawjack = addCreatureReady(player2, new HighRiseSawjack());
        Permanent otherSawjack = addCreatureReady(player2, new HighRiseSawjack());
        Permanent attacker = addReadyAttacker(player1, new SuntailHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(PermanentGrants.powerBoostOf(gd, sawjack)).isZero();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, attacker));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, sawjack)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, sawjack)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, otherSawjack)).isZero();
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }
}
