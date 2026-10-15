package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SungracePegasus.class, RuneclawBear.class})
class SungracePegasusTest extends BaseCardTest {

    @Test
    @DisplayName("Flying creature cannot be blocked by a creature without flying")
    void cannotBeBlockedByNonFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new SungracePegasus());
        attacker.setAttacking(true);

        addCreatureReady(player2, new RuneclawBear());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lifelink gains controller life equal to combat damage dealt")
    void lifelinkGainsLifeOnAttack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new SungracePegasus());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Flying blockers are legal and both Pegasi gain life from creature damage")
    void flyingBlockerAllowsLifelinkForBothControllers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SungracePegasus());
        addCreatureReady(player2, new SungracePegasus());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
        harness.assertOnBattlefield(player1, "Sungrace Pegasus");
        harness.assertOnBattlefield(player2, "Sungrace Pegasus");
    }

    @Test
    @DisplayName("A blocking Pegasus gains life even when combat damage kills it")
    void lifelinkGainsLifeWhenBlockerDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player2, new SungracePegasus());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
        harness.assertInGraveyard(player2, "Sungrace Pegasus");
        harness.assertNotOnBattlefield(player2, "Sungrace Pegasus");
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }
}
