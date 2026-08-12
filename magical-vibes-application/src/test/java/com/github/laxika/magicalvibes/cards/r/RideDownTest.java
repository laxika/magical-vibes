package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RideDownTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the blocker and gives trample to the creatures it blocked")
    void destroysBlockerAndGrantsTrample() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareBlock(attacker, blocker);

        castRideDown(blocker);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Only creatures blocked by the targeted blocker gain trample")
    void onlyTargetBlockersVictimsGainTrample() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherBlocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));

        castRideDown(blocker);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(otherBlocker);
    }

    @Test
    @DisplayName("A creature that is not blocking cannot be targeted")
    void nonBlockingCreatureCannotBeTargeted() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareBlock(attacker, blocker);
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new RideDown()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRideDown(Permanent blocker) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new RideDown()));
        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

}
