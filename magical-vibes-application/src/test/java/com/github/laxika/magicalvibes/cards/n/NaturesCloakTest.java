package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaturesCloak.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class NaturesCloakTest extends BaseCardTest {

    @Test
    @DisplayName("Green creatures you control gain forestwalk; non-green creatures do not")
    void grantsForestwalkToGreenCreaturesOnly() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.castFromHand(player1, new NaturesCloak(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Only affects your own green creatures, not opponent's")
    void doesNotAffectOpponentCreatures() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new NaturesCloak(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, enemyBears, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Forestwalk wears off at end of turn")
    void forestwalkWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new NaturesCloak(), "{2}{G}");
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Green creatures entering after resolution do not gain forestwalk")
    void creaturesEnteringAfterResolutionDoNotGainForestwalk() {
        harness.castFromHand(player1, new NaturesCloak(), "{2}{G}");
        harness.passBothPriorities();

        Permanent laterBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, laterBears, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Granted forestwalk prevents blocking while the defender controls a Forest")
    void forestwalkPreventsBlockingWithForest() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new NaturesCloak(), "{2}{G}");
        harness.passBothPriorities();
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Granted forestwalk allows blocking when the defender controls no Forest")
    void forestwalkAllowsBlockingWithoutForest() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new NaturesCloak(), "{2}{G}");
        harness.passBothPriorities();
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
