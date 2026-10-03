package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RotLikeTheScumYouAre.class, Forest.class})
class RotLikeTheScumYouAreTest extends BaseCardTest {

    @Test
    void createsOozeWithCountersEqualToOpponentsLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        resolveScheme();

        Permanent ooze = findPermanents(player1, "Ooze").stream().findFirst().orElseThrow();
        assertThat(ooze.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(ooze.getEffectivePower()).isEqualTo(5);
        assertThat(ooze.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void doesNotCountControllerLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        resolveScheme();

        Permanent ooze = findPermanents(player1, "Ooze").stream().findFirst().orElseThrow();
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ooze.getEffectivePower()).isEqualTo(2);
        assertThat(ooze.getEffectiveToughness()).isEqualTo(2);
    }

    private void resolveScheme() {
        RotLikeTheScumYouAre scheme = new RotLikeTheScumYouAre();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }
}
