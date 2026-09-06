package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VoraciousVermin;
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

@CardUsed({TatteredRatter.class, GrizzlyBears.class, VoraciousVermin.class})
class TatteredRatterTest extends BaseCardTest {

    @Test
    @DisplayName("A Rat that becomes blocked gets +2/+0 until end of turn")
    void blockedRatGetsBoost() {
        addReady(player1, new TatteredRatter());
        Permanent rat = addReady(player1, new VoraciousVermin());
        rat.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(rat.getPowerModifier()).isEqualTo(2);
        assertThat(rat.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addReady(player1, new TatteredRatter());
        Permanent rat = addReady(player1, new VoraciousVermin());
        rat.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rat.getPowerModifier()).isZero();
        assertThat(rat.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A blocked non-Rat does not get the boost")
    void blockedNonRatDoesNotGetBoost() {
        addReady(player1, new TatteredRatter());
        Permanent bears = addReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A Rat controlled by an opponent does not get this Ratter's boost")
    void opponentRatDoesNotGetThisRattersBoost() {
        addReady(player1, new TatteredRatter());
        addReady(player1, new GrizzlyBears());
        Permanent opponentRat = addReady(player2, new VoraciousVermin());
        opponentRat.setAttacking(true);

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(opponentRat.getPowerModifier()).isZero();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
