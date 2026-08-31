package com.github.laxika.magicalvibes.cards.h;

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
        Permanent sawjack = addReadyCreature(player2, new HighRiseSawjack());
        addReadyAttacker(player1, new SuntailHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(sawjack.getPowerModifier()).isEqualTo(2);
        assertThat(sawjack.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Blocking a creature without flying does not trigger the boost")
    void blockingNonFlyingCreatureDoesNotTriggerBoost() {
        Permanent sawjack = addReadyCreature(player2, new HighRiseSawjack());
        addReadyAttacker(player1, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(sawjack.getPowerModifier()).isZero();
        assertThat(sawjack.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent sawjack = addReadyCreature(player2, new HighRiseSawjack());
        addReadyAttacker(player1, new SuntailHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        assertThat(sawjack.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sawjack.getPowerModifier()).isZero();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent permanent = addReadyCreature(player, card);
        permanent.setAttacking(true);
        return permanent;
    }
}
