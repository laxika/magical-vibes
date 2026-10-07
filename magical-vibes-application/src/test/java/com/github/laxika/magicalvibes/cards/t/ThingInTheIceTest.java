package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.d.DerangedAssistant;
import com.github.laxika.magicalvibes.cards.s.SilentDeparture;
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

@CardUsed({ThingInTheIce.class, ThinkTwice.class, DerangedAssistant.class, SilentDeparture.class})
class ThingInTheIceTest extends BaseCardTest {

    @Test
    void entersWithFourIceCountersWithoutUsingTheStack() {
        harness.castFromHand(player1, new ThingInTheIce(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Thing in the Ice").getCounterCount(CounterType.ICE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fourthInstantTransformsAndBouncesOnlyNonHorrorsBeforeTheSpellResolves() {
        Permanent thing = harness.enterBattlefieldAndReturn(player1, new ThingInTheIce());
        harness.addToBattlefield(player1, new DerangedAssistant());
        harness.addToBattlefield(player2, new DerangedAssistant());
        Permanent otherHorror = harness.enterBattlefieldAndReturn(player2, new ThingInTheIce());

        for (int remaining = 3; remaining >= 0; remaining--) {
            harness.castFromHand(player1, new ThinkTwice(), "{1}{U}");
            harness.passBothPriorities();
            assertThat(thing.getCounterCount(CounterType.ICE)).isEqualTo(remaining);
            assertThat(thing.isTransformed()).isEqualTo(remaining == 0);
            if (remaining == 0) {
                harness.assertOnBattlefield(player1, "Deranged Assistant");
                harness.assertOnBattlefield(player2, "Deranged Assistant");
                harness.passBothPriorities();
                harness.assertInHand(player1, "Deranged Assistant");
                harness.assertInHand(player2, "Deranged Assistant");
                harness.assertNotOnBattlefield(player1, "Deranged Assistant");
                harness.assertNotOnBattlefield(player2, "Deranged Assistant");
                assertThat(otherHorror.isTransformed()).isFalse();
                assertThat(otherHorror.getCounterCount(CounterType.ICE)).isEqualTo(4);
                assertThat(gd.stack).hasSize(1);
            }
            resolveAllTriggers();
        }
        harness.assertOnBattlefield(player1, "Awoken Horror");
        harness.assertOnBattlefield(player2, "Thing in the Ice");
    }

    @Test
    void sorceryAlsoRemovesAnIceCounter() {
        Permanent thing = harness.enterBattlefieldAndReturn(player1, new ThingInTheIce());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DerangedAssistant());
        harness.setHand(player1, List.of(new SilentDeparture()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(thing.getCounterCount(CounterType.ICE)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Deranged Assistant");
        resolveAllTriggers();
        harness.assertInHand(player2, "Deranged Assistant");
    }

    @Test
    void creaturesAndOpponentsInstantsDoNotRemoveCounters() {
        Permanent thing = harness.enterBattlefieldAndReturn(player1, new ThingInTheIce());
        harness.castFromHand(player1, new DerangedAssistant(), "{1}{U}");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.castFromHand(player2, new ThinkTwice(), "{1}{U}");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(thing.getCounterCount(CounterType.ICE)).isEqualTo(4);
        assertThat(thing.isTransformed()).isFalse();
    }

    @Test
    void noCountersDoesNotTransformUntilACastTriggerResolves() {
        Permanent thing = harness.enterBattlefieldAndReturn(player1, new ThingInTheIce());
        thing.setCounterCount(CounterType.ICE, 0);
        harness.runStateBasedActions();
        assertThat(thing.isTransformed()).isFalse();
        harness.castFromHand(player1, new ThinkTwice(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(thing.isTransformed()).isTrue();
        assertThat(thing.getCounterCount(CounterType.ICE)).isZero();
        resolveAllTriggers();
    }

    @Test
    void pendingTriggersCannotTransformAwokenHorrorBackToTheFront() {
        Permanent thing = harness.enterBattlefieldAndReturn(player1, new ThingInTheIce());
        thing.setCounterCount(CounterType.ICE, 1);
        harness.castFromHand(player1, new ThinkTwice(), "{1}{U}");
        harness.castFromHand(player1, new ThinkTwice(), "{1}{U}");
        resolveAllTriggers();

        assertThat(thing.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Awoken Horror");
    }

    @Test
    void bounceReturnsStolenCreaturesToTheirOwner() {
        Permanent thing = harness.enterBattlefieldAndReturn(player1, new ThingInTheIce());
        thing.setCounterCount(CounterType.ICE, 1);
        DerangedAssistant stolenCard = new DerangedAssistant();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, stolenCard);
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        harness.castFromHand(player1, new ThinkTwice(), "{1}{U}");
        resolveAllTriggers();

        harness.assertInHand(player2, "Deranged Assistant");
        harness.assertNotInHand(player1, "Deranged Assistant");
        harness.assertNotOnBattlefield(player1, "Deranged Assistant");
    }

    @Test
    void counterCheckUsesTheCountAfterRemovalAtResolution() {
        Permanent thing = harness.enterBattlefieldAndReturn(player1, new ThingInTheIce());
        thing.setCounterCount(CounterType.ICE, 1);
        harness.castFromHand(player1, new ThinkTwice(), "{1}{U}");
        thing.setCounterCount(CounterType.ICE, 2);
        harness.passBothPriorities();

        assertThat(thing.getCounterCount(CounterType.ICE)).isEqualTo(1);
        assertThat(thing.isTransformed()).isFalse();
        resolveAllTriggers();
    }

    @Test
    void transformedFaceDoesNotTriggerForLaterInstants() {
        Permanent horror = addTransformedAwokenHorror();
        harness.castFromHand(player1, new ThinkTwice(), "{1}{U}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(horror.isTransformed()).isTrue();
    }

    @Test
    void frontFaceCannotAttack() {
        addCreatureReady(player1, new ThingInTheIce());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Awoken Horror can attack after transforming")
    void awokenHorrorCanAttackAfterTransforming() {
        addTransformedAwokenHorror();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        // Declaring is itself the assertion: an illegal attacker throws here. Combat then runs to
        // the damage step on its own, so the life loss is what proves the attack connected.
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(lifeBefore);
    }

    /**
     * Puts Thing in the Ice onto the battlefield already flipped, swapping in the very back-face
     * card instance the engine's own transform path uses ({@code originalCard.getBackFaceCard()}).
     */
    private Permanent addTransformedAwokenHorror() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new ThingInTheIce());
        Card backFace = perm.getOriginalCard().getBackFaceCard();
        perm.setCard(backFace);
        perm.setTransformed(true);
        perm.setSummoningSick(false);
        return perm;
    }
}
