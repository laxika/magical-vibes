package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MacCreadyLamplightMayor.class, GrizzlyBears.class, HillGiant.class, AirElemental.class})
class MacCreadyLamplightMayorTest extends BaseCardTest {

    @Test
    @DisplayName("A small attacking creature gains skulk until end of turn")
    void smallAttackerGainsSkulk() {
        addCreatureReady(player1, new MacCreadyLamplightMayor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.SKULK)).isTrue();
    }

    @Test
    @DisplayName("The skulk trigger does not trigger for a creature with power 3")
    void skulkTriggerRequiresPowerTwoOrLess() {
        addCreatureReady(player1, new MacCreadyLamplightMayor());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature with power 4 or greater attacking directly drains its controller")
    void largeAttackerDrainsItsController() {
        addCreatureReady(player1, new MacCreadyLamplightMayor());
        addCreatureReady(player2, new AirElemental());
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        declareAttackers(player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 2);
    }

    @Test
    @DisplayName("The drain trigger does not trigger for a creature with power 3")
    void drainTriggerRequiresPowerFourOrGreater() {
        addCreatureReady(player1, new MacCreadyLamplightMayor());
        addCreatureReady(player2, new HillGiant());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }
}
