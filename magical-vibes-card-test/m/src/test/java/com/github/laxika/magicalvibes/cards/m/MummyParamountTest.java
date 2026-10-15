package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.cards.k.KhenraEternal;
import com.github.laxika.magicalvibes.cards.s.SteadfastSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MummyParamount.class, KhenraEternal.class, FeralProwler.class, SteadfastSentinel.class})
class MummyParamountTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when another Zombie enters")
    void getsBoostWhenZombieEnters() {
        Permanent paramount = harness.addToBattlefieldAndReturn(player1, new MummyParamount());

        harness.castFromHand(player1, new KhenraEternal(), "{1}{B}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, paramount)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when a non-Zombie creature enters")
    void noBoostWhenNonZombieEnters() {
        Permanent paramount = harness.addToBattlefieldAndReturn(player1, new MummyParamount());

        harness.castFromHand(player1, new FeralProwler(), "{1}{G}");
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paramount)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's Zombie enters")
    void noBoostWhenOpponentZombieEnters() {
        Permanent paramount = harness.addToBattlefieldAndReturn(player1, new MummyParamount());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new KhenraEternal(), "{1}{B}");
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paramount)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost is cumulative across multiple Zombie entries")
    void boostStacksForMultipleZombies() {
        Permanent paramount = harness.addToBattlefieldAndReturn(player1, new MummyParamount());

        harness.castFromHand(player1, new KhenraEternal(), "{1}{B}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(3);

        harness.castFromHand(player1, new KhenraEternal(), "{1}{B}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, paramount)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent paramount = harness.addToBattlefieldAndReturn(player1, new MummyParamount());

        harness.castFromHand(player1, new KhenraEternal(), "{1}{B}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paramount)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void doesNotTriggerForOwnEntry() {
        harness.castFromHand(player1, new MummyParamount(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent paramount = findPermanent(player1, "Mummy Paramount");
        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paramount)).isEqualTo(2);
    }

    @Test
    @DisplayName("A second Mummy Paramount boosts only the one already on the battlefield")
    void secondParamountBoostsOnlyExistingParamount() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MummyParamount());

        harness.castFromHand(player1, new MummyParamount(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        harness.passBothPriorities();

        Permanent second = findPermanents(player1, "Mummy Paramount").get(1);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers when a Zombie enters without being cast")
    void triggersForZombiePutOntoBattlefield() {
        Permanent paramount = harness.addToBattlefieldAndReturn(player1, new MummyParamount());

        harness.enterBattlefieldAndReturn(player1, new KhenraEternal());

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, paramount)).isEqualTo(3);
    }

    @Test
    @DisplayName("Triggers for an eternalized Zombie token")
    void triggersForEternalizedZombieToken() {
        Permanent paramount = harness.addToBattlefieldAndReturn(player1, new MummyParamount());
        harness.setGraveyard(player1, List.of(new SteadfastSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paramount)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, paramount)).isEqualTo(3);
    }
}
