package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KronchWrangler.class, AirElemental.class, GrizzlyBears.class})
class KronchWranglerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when a power-4 creature you control enters")
    void putsCounterOnSelfForPowerFourAlly() {
        Permanent kronch = addCreatureReady(player1, new KronchWrangler());

        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(kronch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a creature with power less than 4")
    void doesNotTriggerForSmallAlly() {
        Permanent kronch = addCreatureReady(player1, new KronchWrangler());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(kronch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger for a power-4 creature controlled by an opponent")
    void doesNotTriggerForOpponentCreature() {
        Permanent kronch = addCreatureReady(player1, new KronchWrangler());
        harness.setHand(player1, List.of());

        harness.setHand(player2, List.of(new AirElemental()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(kronch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
