package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NaturesCloakTest extends BaseCardTest {

    @Test
    @DisplayName("Green creatures you control gain forestwalk; non-green creatures do not")
    void grantsForestwalkToGreenCreaturesOnly() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new NaturesCloak()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Only affects your own green creatures, not opponent's")
    void doesNotAffectOpponentCreatures() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NaturesCloak()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, enemyBears, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Forestwalk wears off at end of turn")
    void forestwalkWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NaturesCloak()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isFalse();
    }
}
