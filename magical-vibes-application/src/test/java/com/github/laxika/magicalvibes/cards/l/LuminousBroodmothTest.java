package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StarlitAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LuminousBroodmoth.class, GrizzlyBears.class, StarlitAngel.class})
class LuminousBroodmothTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature without flying with a flying counter")
    void returnsCreatureWithoutFlyingWithFlyingCounter() {
        harness.addToBattlefield(player1, new LuminousBroodmoth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        kill(creature);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return a creature that had flying")
    void doesNotReturnCreatureWithFlying() {
        harness.addToBattlefield(player1, new LuminousBroodmoth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StarlitAngel());

        kill(creature);

        harness.assertNotOnBattlefield(player1, "Starlit Angel");
        harness.assertInGraveyard(player1, "Starlit Angel");
    }

    private void kill(Permanent creature) {
        creature.setMarkedDamage(creature.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }
}
