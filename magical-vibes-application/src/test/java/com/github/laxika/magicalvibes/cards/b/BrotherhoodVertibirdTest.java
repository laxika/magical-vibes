package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrotherhoodVertibird.class, GrizzlyBears.class, Ornithopter.class})
class BrotherhoodVertibirdTest extends BaseCardTest {

    @Test
    void powerEqualsControlledArtifactsAndToughnessIsFour() {
        Permanent vertibird = harness.addToBattlefieldAndReturn(player1, new BrotherhoodVertibird());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, vertibird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vertibird)).isEqualTo(4);

        harness.addToBattlefield(player1, new Ornithopter());
        assertThat(gqs.getEffectivePower(gd, vertibird)).isEqualTo(4);
    }

    @Test
    void crewAnimatesVertibirdAndTapsCreaturesWithTotalPowerTwo() {
        Permanent vertibird = harness.addToBattlefieldAndReturn(player1, new BrotherhoodVertibird());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vertibird)).isTrue();
        assertThat(bears.isTapped()).isTrue();
    }
}
