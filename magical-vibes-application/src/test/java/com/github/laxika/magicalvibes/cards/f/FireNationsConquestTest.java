package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireNationsConquest.class, GrizzlyBears.class, Opalescence.class})
class FireNationsConquestTest extends BaseCardTest {

    @Test
    void boostsCreaturesYouControl() {
        harness.addToBattlefield(player1, new FireNationsConquest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void doesNotBoostOpponentsCreatures() {
        harness.addToBattlefield(player1, new FireNationsConquest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void bonusIsRemovedWhenSourceLeavesBattlefield() {
        Permanent conquest = harness.addToBattlefieldAndReturn(player1, new FireNationsConquest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(conquest);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void boostsExistingCreaturesAfterResolving() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new FireNationsConquest(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fire Nation's Conquest");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void multipleCopiesStackTheirBonuses() {
        harness.addToBattlefield(player1, new FireNationsConquest());
        harness.addToBattlefield(player1, new FireNationsConquest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void boostsItselfWhenItBecomesACreature() {
        Permanent conquest = harness.addToBattlefieldAndReturn(player1, new FireNationsConquest());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, conquest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, conquest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, conquest)).isEqualTo(3);
    }
}
