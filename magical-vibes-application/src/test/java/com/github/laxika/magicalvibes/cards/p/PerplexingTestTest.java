package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SliverQueen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerplexingTest.class, GrizzlyBears.class, SliverQueen.class})
class PerplexingTestTest extends BaseCardTest {

    @Test
    void returnsAllCreatureTokensAndLeavesNontokenCreatures() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new SliverQueen());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        createSliverToken();

        cast(0);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyInAnyOrder(queen, bear);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.isToken());
    }

    @Test
    void returnsAllNontokenCreaturesAndLeavesCreatureTokens() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new SliverQueen());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        createSliverToken();

        cast(1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(findToken());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactlyInAnyOrder(SliverQueen.class, GrizzlyBears.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(queen, bear);
    }

    private void createSliverToken() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent findToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new PerplexingTest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalInstant(player1, 0, modeIndex, List.of());
        harness.passBothPriorities();
    }
}
