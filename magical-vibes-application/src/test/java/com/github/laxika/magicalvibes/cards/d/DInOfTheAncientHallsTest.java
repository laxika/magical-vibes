package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.StaunchShieldmate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DInOfTheAncientHalls.class, StaunchShieldmate.class})
class DInOfTheAncientHallsTest extends BaseCardTest {

    @Test
    void attackingDealsDamageEqualToDwarvesYouControlToEachOpponent() {
        addCreatureReady(player1, new DInOfTheAncientHalls());
        addCreatureReady(player1, new StaunchShieldmate());
        addCreatureReady(player2, new StaunchShieldmate());

        int lifeBefore = gd.getLife(player2.getId());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void countsDwarvesAtResolution() {
        addCreatureReady(player1, new DInOfTheAncientHalls());
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        addCreatureReady(player1, new StaunchShieldmate());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
    }
}
