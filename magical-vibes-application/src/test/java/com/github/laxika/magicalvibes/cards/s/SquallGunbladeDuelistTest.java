package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SquallGunbladeDuelist.class, GrizzlyBears.class})
class SquallGunbladeDuelistTest extends BaseCardTest {

    @Test
    void matchingAttackerDealsSquallsPowerToDefendingPlayer() {
        Permanent squall = addCreatureReady(player1, new SquallGunbladeDuelist());
        squall.setChosenNumber(2);
        addCreatureReady(player1, new GrizzlyBears());

        int lifeBefore = gd.getLife(player2.getId());
        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    void nonmatchingAttackerDoesNotTrigger() {
        Permanent squall = addCreatureReady(player1, new SquallGunbladeDuelist());
        squall.setChosenNumber(3);
        addCreatureReady(player1, new GrizzlyBears());

        int lifeBefore = gd.getLife(player2.getId());
        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }
}
