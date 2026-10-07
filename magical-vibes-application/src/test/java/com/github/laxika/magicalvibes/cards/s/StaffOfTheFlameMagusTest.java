package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StaffOfTheFlameMagus.class, RagingGoblin.class, GrizzlyBears.class, Mountain.class, Island.class})
class StaffOfTheFlameMagusTest extends BaseCardTest {

    private void addStaff() {
        harness.addToBattlefield(player1, new StaffOfTheFlameMagus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Gains 1 life when you cast a red spell")
    void gainsLifeOnRedSpell() {
        addStaff();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        harness.passBothPriorities(); // resolve cast trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not gain life when you cast a non-red spell")
    void noLifeOnNonRedSpell() {
        addStaff();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Gains 1 life when a Mountain you control enters")
    void gainsLifeWhenMountainEnters() {
        addStaff();
        harness.setHand(player1, List.of(new Mountain()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not gain life when a non-Mountain land enters")
    void noLifeOnNonMountainLand() {
        addStaff();
        harness.setHand(player1, List.of(new Island()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent casting a red spell does not trigger")
    void opponentRedSpellDoesNotTrigger() {
        addStaff();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new RagingGoblin(), "{R}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent's Mountain entering does not trigger")
    void opponentMountainDoesNotTrigger() {
        addStaff();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Mountain()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("A Mountain entering without being played still triggers")
    void mountainPutOntoBattlefieldTriggers() {
        addStaff();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new Mountain());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Each Staff triggers independently for the same red spell")
    void multipleStaffsEachGainLife() {
        addStaff();
        harness.addToBattlefield(player1, new StaffOfTheFlameMagus());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.stack).hasSize(1);
    }
}
