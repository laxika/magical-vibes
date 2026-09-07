package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlaughterSpecialist.class, Assassinate.class})
class SlaughterSpecialistTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent creates a 1/1 white Human token when it enters")
    void eachOpponentCreatesHumanToken() {
        castSpecialist();

        assertThat(countPermanents(player1, "Human")).isZero();
        assertThat(countPermanents(player2, "Human")).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent's creature dies")
    void gainsCounterWhenOpponentCreatureDies() {
        Permanent specialist = castSpecialist();
        Permanent human = findPermanent(player2, "Human");
        human.tap();

        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, human.getId());
        resolveAllTriggers();

        assertThat(specialist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castSpecialist() {
        harness.setHand(player1, List.of(new SlaughterSpecialist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Slaughter Specialist");
    }
}
