package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
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

@CardUsed({VampireSocialite.class, CaptivatingVampire.class, GrizzlyBears.class})
class VampireSocialiteTest extends BaseCardTest {

    @Test
    @DisplayName("When an opponent lost life, its entry puts a counter on each other Vampire you control")
    void entryCountersOtherVampiresAfterOpponentLostLife() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        Permanent nonVampire = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        castSocialite();
        harness.passBothPriorities();

        Permanent socialite = findPermanent(player1, "Vampire Socialite");
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(socialite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Its entry does nothing when no opponent lost life this turn")
    void entryDoesNothingWithoutOpponentLifeLoss() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());

        castSocialite();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Vampire Socialite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An entering Vampire gets an additional counter while an opponent has lost life")
    void enteringVampireGetsAdditionalCounter() {
        harness.addToBattlefield(player1, new VampireSocialite());
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        harness.setHand(player1, List.of(new CaptivatingVampire()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Captivating Vampire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The entering-counter effect does not apply without opponent life loss")
    void enteringVampireDoesNotGetAdditionalCounterWithoutOpponentLifeLoss() {
        harness.addToBattlefield(player1, new VampireSocialite());

        harness.setHand(player1, List.of(new CaptivatingVampire()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Captivating Vampire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castSocialite() {
        harness.setHand(player1, List.of(new VampireSocialite()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
