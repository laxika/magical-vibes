package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MomentOfCraving;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PaladinOfAtonement.class, Shock.class, MomentOfCraving.class})
class PaladinOfAtonementTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself during each upkeep after its controller lost life")
    void growsDuringUpkeepAfterControllerLostLife() {
        harness.addToBattlefield(player1, new PaladinOfAtonement());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        resolveAllTriggers();

        Permanent paladin = findPermanent(player1, "Paladin of Atonement");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself during upkeep without life loss last turn")
    void doesNotGrowWithoutControllerLifeLoss() {
        harness.addToBattlefield(player1, new PaladinOfAtonement());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Paladin of Atonement");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gains life equal to its toughness when it dies")
    void gainsLifeEqualToToughnessOnDeath() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new PaladinOfAtonement());
        paladin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, paladin.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Grows during the opponent's upkeep after losing life on its controller's turn")
    void growsDuringOpponentsUpkeepButNotAnotherTurnLater() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new PaladinOfAtonement());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        resolveAllTriggers();
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gd.stack).isEmpty();
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger from the opponent losing life last turn")
    void ignoresOpponentsLifeLoss() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new PaladinOfAtonement());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Life loss during the current upkeep cannot make the ability trigger retroactively")
    void ignoresLifeLossDuringCurrentUpkeep() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new PaladinOfAtonement());
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Grows even if it entered after the controller lost life and later gained more life")
    void growsAfterEarlierLifeLossDespiteNetLifeGain() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new PaladinOfAtonement());
        harness.setHand(player1, List.of(new MomentOfCraving(), new MomentOfCraving()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addToBattlefield(player2, new PaladinOfAtonement());
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Paladin of Atonement"));
        resolveAllTriggers();
        harness.addToBattlefield(player2, new PaladinOfAtonement());
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Paladin of Atonement"));
        resolveAllTriggers();
        harness.assertLife(player1, 22);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        resolveAllTriggers();

        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dying with negative toughness neither gains nor loses life")
    void gainsNoLifeWithNegativeToughness() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new PaladinOfAtonement());
        harness.setHand(player2, List.of(new MomentOfCraving()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, paladin.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Paladin of Atonement");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Dying with zero toughness gains no life despite its printed toughness and counters")
    void usesReducedLastKnownToughnessOnDeath() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new PaladinOfAtonement());
        paladin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new MomentOfCraving()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, paladin.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Paladin of Atonement");
        harness.assertLife(player1, 20);
    }
}
