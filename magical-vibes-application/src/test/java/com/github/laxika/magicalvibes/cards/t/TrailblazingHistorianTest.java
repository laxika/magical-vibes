package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrailblazingHistorian.class, GrizzlyBears.class})
class TrailblazingHistorianTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Trailblazing Historian gives another target creature haste")
    void givesAnotherCreatureHaste() {
        addCreatureReady(player1, new TrailblazingHistorian());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void grantedHasteWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new TrailblazingHistorian());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target Trailblazing Historian itself")
    void cannotTargetItself() {
        Permanent historian = addCreatureReady(player1, new TrailblazingHistorian());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, historian.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");
    }

    @Test
    @DisplayName("Its own haste allows the tap ability immediately after entering")
    void canActivateImmediatelyAfterEntering() {
        Permanent historian = harness.addToBattlefieldAndReturn(player1, new TrailblazingHistorian());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());

        assertThat(historian.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can grant haste to an opponent's creature")
    void canTargetOpponentsCreature() {
        addCreatureReady(player1, new TrailblazingHistorian());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent historian = addCreatureReady(player1, new TrailblazingHistorian());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(historian);
        gd.playerGraveyards.get(player1.getId()).add(historian.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A target that leaves before resolution does not gain haste")
    void removedTargetDoesNotGainHaste() {
        Permanent historian = addCreatureReady(player1, new TrailblazingHistorian());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerGraveyards.get(player1.getId()).add(bears.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(historian.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
