package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TuktukRubblefort.class, GrizzlyBears.class})
class TuktukRubblefortTest extends BaseCardTest {

    @Test
    @DisplayName("Grants haste to creatures its controller controls, including itself")
    void grantsHasteToOwnCreatures() {
        Permanent rubblefort = addCreatureReady(player1, new TuktukRubblefort());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, rubblefort, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant haste to an opponent's creatures")
    void doesNotGrantHasteToOpponentsCreatures() {
        harness.addToBattlefield(player1, new TuktukRubblefort());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after Rubblefort can attack immediately")
    void grantsHasteToCreaturesEnteringLater() {
        harness.enterBattlefieldAndReturn(player1, new TuktukRubblefort());
        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(bears.isSummoningSick()).isTrue();
        assertThat(als.canAttack(gd, bears, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Creatures lose haste immediately when Rubblefort leaves the battlefield")
    void losesHasteWhenRubblefortLeaves() {
        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(als.canAttack(gd, bears, player1.getId())).isFalse();

        Permanent rubblefort = harness.enterBattlefieldAndReturn(player1, new TuktukRubblefort());
        assertThat(als.canAttack(gd, bears, player1.getId())).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, rubblefort));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(als.canAttack(gd, bears, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Rubblefort's haste does not let it attack despite defender")
    void hasteDoesNotOverrideDefender() {
        Permanent rubblefort = harness.enterBattlefieldAndReturn(player1, new TuktukRubblefort());

        assertThat(gqs.hasKeyword(gd, rubblefort, Keyword.HASTE)).isTrue();
        assertThat(als.canAttack(gd, rubblefort, player1.getId())).isFalse();
    }
}
