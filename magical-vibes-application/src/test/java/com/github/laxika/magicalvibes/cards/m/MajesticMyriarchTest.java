package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MajesticMyriarchTest extends BaseCardTest {

    private Permanent addMyriarch(Player player) {
        Permanent perm = new Permanent(new MajesticMyriarch());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void endTurn() {
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("P/T is 2/2 when it is your only creature")
    void isTwoTwoWhenAlone() {
        Permanent myriarch = addMyriarch(player1);

        assertThat(gqs.getEffectivePower(gd, myriarch)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, myriarch)).isEqualTo(2);
    }

    @Test
    @DisplayName("P/T is twice the number of creatures you control")
    void ptIsTwiceControlledCreatures() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, myriarch)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, myriarch)).isEqualTo(6);
    }

    @Test
    @DisplayName("Opponent creatures do not count toward P/T")
    void opponentCreaturesDoNotCount() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, myriarch)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, myriarch)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains flying until end of turn when you control a creature with flying")
    void gainsFlyingFromAllyFlier() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player1, new SerraAngel());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not gain flying when only the opponent controls a flier")
    void opponentFlierDoesNotGrant() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player2, new SerraAngel());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not gain keywords when you control no matching creatures")
    void noKeywordsWithoutMatchers() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player1, new SerraAngel());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Triggers during the opponent's combat as well")
    void triggersDuringOpponentCombat() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player1, new SerraAngel());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isTrue();
    }
}
