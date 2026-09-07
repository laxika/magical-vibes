package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DustAnimus.class, Plains.class})
class DustAnimusTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters and a lifelink counter with five untapped lands")
    void entersWithCountersAndLifelinkWithFiveUntappedLands() {
        addPlains(5);

        castDustAnimus();

        Permanent animus = findPermanents(player1, "Dust Animus").getFirst();
        assertThat(animus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(animus.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not enter with counters when one of five lands is tapped")
    void doesNotEnterWithCountersWhenARequiredLandIsTapped() {
        addPlains(4);
        Permanent tappedPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        tappedPlains.tap();

        castDustAnimus();

        Permanent animus = findPermanents(player1, "Dust Animus").getFirst();
        assertThat(animus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(animus.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    @DisplayName("Opponent's untapped lands do not count")
    void doesNotCountOpponentsLands() {
        addPlains(4);
        harness.addToBattlefield(player2, new Plains());

        castDustAnimus();

        Permanent animus = findPermanents(player1, "Dust Animus").getFirst();
        assertThat(animus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(animus.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    private void addPlains(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Plains());
        }
    }

    private void castDustAnimus() {
        harness.setHand(player1, List.of(new DustAnimus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
