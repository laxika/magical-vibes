package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RollingHamsphere.class, GrizzlyBears.class})
class RollingHamsphereTest extends BaseCardTest {

    @Test
    void attackingCreatesHamstersAndDealsDamageEqualToHamstersControlled() {
        Permanent sphere = addReadySphere();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(findPermanents(player1, "Hamster")).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, sphere)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(7);
    }

    private Permanent addReadySphere() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new RollingHamsphere());
        sphere.setSummoningSick(false);
        return sphere;
    }
}
