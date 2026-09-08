package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrostwindInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain flying until end of turn")
    void ownCreaturesGainFlyingUntilEndOfTurn() {
        Permanent invoker = addCreatureReady(player1, new FrostwindInvoker());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(invoker), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The flying granted by Frostwind Invoker wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent invoker = addCreatureReady(player1, new FrostwindInvoker());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(invoker), null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.FLYING)).isFalse();
    }
}
