package com.github.laxika.magicalvibes.cards.t;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheArtOfTea.class, GrizzlyBears.class})
class TheArtOfTeaTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a controlled creature and creates a Food")
    void putsCounterAndCreatesFood() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castTea(bear.getId());

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a Food when no creature is chosen")
    void createsFoodWithoutTarget() {
        castTea();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheArtOfTea()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTea() {
        harness.setHand(player1, List.of(new TheArtOfTea()));
        addMana();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void castTea(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new TheArtOfTea()));
        addMana();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
