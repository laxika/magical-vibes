package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SliverQueen;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerplexingTest.class, GrizzlyBears.class, SliverQueen.class, SolRing.class})
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
                .containsExactly(findPermanent(player1, "Sliver"));
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

    @Test
    void tokenModeReturnsOpponentsTokensButLeavesTheirOtherPermanents() {
        Permanent queen = harness.addToBattlefieldAndReturn(player2, new SliverQueen());
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player2, "Sliver");

        cast(0);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactlyInAnyOrder(queen, ring);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(token.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(token.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(token.getCard());
    }

    @Test
    void nontokenModeReturnsCreaturesToOwnersAcrossBothBattlefields() {
        GrizzlyBears stolenBear = new GrizzlyBears();
        stolenBear.setOwnerId(player1.getId());
        SliverQueen opposingQueen = new SliverQueen();
        opposingQueen.setOwnerId(player2.getId());
        harness.addToBattlefield(player2, stolenBear);
        harness.addToBattlefield(player2, opposingQueen);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());

        cast(1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(ring);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(stolenBear, ownBear.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opposingQueen);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherModeResolvesWithoutMatchingCreatures(int modeIndex) {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());

        cast(modeIndex);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ring);
        harness.assertInGraveyard(player1, "Perplexing Test");
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new PerplexingTest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalInstant(player1, 0, modeIndex, List.of());
        harness.passBothPriorities();
    }
}
