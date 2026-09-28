package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicSleuth.class, GrizzlyBears.class, Unsummon.class})
class AngelicSleuthTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when another permanent you control leaves with a counter")
    void investigatesWhenCounteredPermanentLeaves() {
        harness.addToBattlefield(player1, new AngelicSleuth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        bouncePermanent(bears, player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not investigate when the departing permanent has no counters")
    void doesNotInvestigateWithoutCounters() {
        harness.addToBattlefield(player1, new AngelicSleuth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        bouncePermanent(bears, player1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Does not investigate when an opponent's permanent leaves")
    void doesNotInvestigateForOpponentPermanent() {
        harness.addToBattlefield(player1, new AngelicSleuth());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        bouncePermanent(bears, player1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private void bouncePermanent(Permanent permanent, com.github.laxika.magicalvibes.model.Player caster) {
        harness.setHand(caster, List.of(new Unsummon()));
        harness.addMana(caster, ManaColor.BLUE, 1);
        harness.castInstant(caster, 0, permanent.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
