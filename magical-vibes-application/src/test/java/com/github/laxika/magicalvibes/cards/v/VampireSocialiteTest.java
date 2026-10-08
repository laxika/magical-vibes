package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
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

@CardUsed({VampireSocialite.class, CaptivatingVampire.class, GrizzlyBears.class,
        ArtificialEvolution.class, Bitterblossom.class})
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

        harness.castFromHand(player1, new CaptivatingVampire(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Captivating Vampire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The entering-counter effect does not apply without opponent life loss")
    void enteringVampireDoesNotGetAdditionalCounterWithoutOpponentLifeLoss() {
        harness.addToBattlefield(player1, new VampireSocialite());

        harness.castFromHand(player1, new CaptivatingVampire(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Captivating Vampire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void controllerLifeLossDoesNotEnableEitherAbility() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        castSocialite();

        assertThat(gd.stack).isEmpty();
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new VampireSocialite());

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondSocialiteGetsEntryCounterAndCountersOnlyTheFirstOnResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new VampireSocialite());
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        castSocialite();

        Permanent second = findPermanents(player1, "Vampire Socialite").getLast();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void multipleSocialitesApplyIndependentEntryCounters() {
        harness.addToBattlefield(player1, new VampireSocialite());
        harness.addToBattlefield(player1, new VampireSocialite());
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        harness.castFromHand(player1, new CaptivatingVampire(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Captivating Vampire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void neitherAbilityCountersOpponentsVampiresOrEnteringNonVampires() {
        Permanent opponentVampire = harness.addToBattlefieldAndReturn(player2, new VampireSocialite());
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        castSocialite();
        harness.passBothPriorities();

        Permanent enteringOpponentVampire = harness.enterBattlefieldAndReturn(player2, new CaptivatingVampire());
        Permanent nonVampire = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(opponentVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(enteringOpponentVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nonVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void enteringNoncreatureVampireGetsAdditionalCounter() {
        harness.addToBattlefield(player1, new VampireSocialite());
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        harness.castFromHand(player1, new Bitterblossom(), "{1}{B}");
        var spellId = gd.stack.getFirst().getCard().getId();
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, spellId);
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "VAMPIRE");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bitterblossom")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void netLifeGainDoesNotDisableEitherAbilityAfterLifeLoss() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player2.getId(), 2);
            harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test setup");
        });

        castSocialite();
        harness.passBothPriorities();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castFromHand(player1, new CaptivatingVampire(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Captivating Vampire").getLast()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void entryTriggerCountersExistingNoncreatureVampires() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, enchantment.getId());
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "VAMPIRE");
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        castSocialite();
        harness.passBothPriorities();

        assertThat(enchantment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castSocialite() {
        harness.castFromHand(player1, new VampireSocialite(), "{B}{R}");
        harness.passBothPriorities();
    }
}
