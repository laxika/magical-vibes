package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BenalishHero;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrcishVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Can't block white creatures with power 2 or greater, but can block other creatures")
    void restrictsBlockingByColorAndPower() {
        Permanent veteran = addVeteran();
        Permanent whiteTwoPower = new Permanent(new WhiteKnight());
        Permanent whiteOnePower = new Permanent(new BenalishHero());
        Permanent nonwhiteTwoPower = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(whiteTwoPower);
        gd.playerBattlefields.get(player2.getId()).add(whiteOnePower);
        gd.playerBattlefields.get(player2.getId()).add(nonwhiteTwoPower);

        assertThat(bls.canBlockAttacker(gd, veteran, whiteTwoPower,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, veteran, whiteOnePower,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, veteran, nonwhiteTwoPower,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("The activated ability grants first strike until end of turn")
    void gainsFirstStrikeUntilEndOfTurn() {
        Permanent veteran = addVeteran();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addVeteran() {
        Permanent veteran = new Permanent(new OrcishVeteran());
        veteran.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(veteran);
        return veteran;
    }
}
