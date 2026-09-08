package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DuskhunterBat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LagacLizard;
import com.github.laxika.magicalvibes.cards.r.RatColony;
import com.github.laxika.magicalvibes.cards.s.SquirrelMob;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValleyRotcaller.class, SquirrelMob.class, DuskhunterBat.class, LagacLizard.class,
        RatColony.class, GrizzlyBears.class})
class ValleyRotcallerTest extends BaseCardTest {

    @Test
    void attacksAndDrainsForOtherMatchingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ValleyRotcaller());
        addCreatureReady(player1, new SquirrelMob());
        addCreatureReady(player1, new DuskhunterBat());
        addCreatureReady(player1, new LagacLizard());
        addCreatureReady(player1, new RatColony());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new SquirrelMob());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
    }

    @Test
    void sourceAndOpponentsAreNotCounted() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ValleyRotcaller());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new SquirrelMob());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void countIsEvaluatedAsTheTriggerResolves() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ValleyRotcaller());
        addCreatureReady(player1, new SquirrelMob());

        declareAttackers(player1, List.of(0));
        harness.addToBattlefield(player1, new LagacLizard());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }
}
