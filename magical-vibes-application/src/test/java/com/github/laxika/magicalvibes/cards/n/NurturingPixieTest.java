package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WillowFaerie;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NurturingPixie.class, GrizzlyBears.class, Island.class, WillowFaerie.class})
class NurturingPixieTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a non-Faerie nonland permanent and gets a +1/+1 counter")
    void returnsEligiblePermanentAndGetsCounter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NurturingPixie()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent pixie = findPermanent(player1, "Nurturing Pixie");
        assertThat(pixie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can decline the optional target")
    void canDeclineTarget() {
        harness.setHand(player1, List.of(new NurturingPixie()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent pixie = findPermanent(player1, "Nurturing Pixie");
        assertThat(pixie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a land or a Faerie")
    void rejectsLandAndFaerieTargets() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new NurturingPixie()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Faerie, nonland permanent you control");

        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new WillowFaerie());
        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, faerie.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Faerie, nonland permanent you control");
    }
}
