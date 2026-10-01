package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JackedRabbit.class, GrizzlyBears.class})
class JackedRabbitTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous puts X counters on Jacked Rabbit and draws at X=5")
    void ravenousAtThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castJackedRabbit(5);

        Permanent rabbit = findPermanent(player1, "Jacked Rabbit");
        assertThat(rabbit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Ravenous does not draw below X=5")
    void ravenousBelowThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castJackedRabbit(4);

        assertThat(findPermanent(player1, "Jacked Rabbit")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking creates Rabbit tokens equal to Jacked Rabbit's power")
    void attackingCreatesRabbitsEqualToPower() {
        castJackedRabbit(2);
        Permanent rabbit = findPermanent(player1, "Jacked Rabbit");
        rabbit.setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(rabbit.getEffectivePower()).isEqualTo(3);
        List<Permanent> tokens = findPermanents(player1, "Rabbit");
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.RABBIT);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    private void castJackedRabbit(int xValue) {
        harness.setHand(player1, List.of(new JackedRabbit()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);

        harness.castCreature(player1, 0, xValue);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
