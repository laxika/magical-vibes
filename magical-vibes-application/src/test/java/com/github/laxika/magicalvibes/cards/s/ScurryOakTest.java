package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScurryOak.class, CentaurCourser.class})
class ScurryOakTest extends BaseCardTest {

    @Test
    void evolvesAndMayCreateSquirrel() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new ScurryOak());
        castCreature(new CentaurCourser());

        assertThat(oak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Squirrel")).isEqualTo(1);
    }

    @Test
    void decliningDoesNotCreateSquirrel() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new ScurryOak());
        castCreature(new CentaurCourser());

        assertThat(oak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Squirrel")).isZero();
    }

    @Test
    void doesNotEvolveForCreatureThatIsNotBigger() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new ScurryOak());
        castCreature(new ScurryOak());

        assertThat(oak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castCreature(Card creature) {
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
