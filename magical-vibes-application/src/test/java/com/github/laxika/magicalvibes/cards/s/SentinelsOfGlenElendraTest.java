package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SentinelsOfGlenElendra.class, HillcomberGiant.class})
class SentinelsOfGlenElendraTest extends BaseCardTest {

    @Test
    void flashAllowsCastingOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SentinelsOfGlenElendra()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passPriority(player2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent sentinels = addCreatureReady(player1, new SentinelsOfGlenElendra());
        Permanent blocker = addCreatureReady(player2, new HillcomberGiant());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(sentinels)));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sentinels);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flashAllowsCastingDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new SentinelsOfGlenElendra()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sentinels of Glen Elendra");
        harness.assertNotInHand(player1, "Sentinels of Glen Elendra");
    }

    @Test
    void flyingCreatureCanBlockSentinels() {
        addCreatureReady(player1, new SentinelsOfGlenElendra());
        Permanent blocker = addCreatureReady(player2, new SentinelsOfGlenElendra());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void sentinelsCanBlockGroundCreature() {
        addCreatureReady(player1, new HillcomberGiant());
        Permanent blocker = addCreatureReady(player2, new SentinelsOfGlenElendra());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
