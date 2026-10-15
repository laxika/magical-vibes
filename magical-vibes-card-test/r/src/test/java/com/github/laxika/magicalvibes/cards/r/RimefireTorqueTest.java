package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SilvergillPeddler;
import com.github.laxika.magicalvibes.cards.s.SurlyFarrier;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RimefireTorque.class, LightningBolt.class, SilvergillPeddler.class, SurlyFarrier.class})
class RimefireTorqueTest extends BaseCardTest {

    @Test
    @DisplayName("A permanent entering before the subtype is chosen does not trigger")
    void enteringBeforeSubtypeChoiceDoesNotTrigger() {
        Permanent torque = addTorque(null, 0);
        harness.castFromHand(player1, new SilvergillPeddler(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(torque.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("A permanent you control of the chosen type adds a charge counter")
    void matchingPermanentAddsChargeCounter() {
        Permanent torque = addTorque(CardSubtype.MERFOLK, 0);

        harness.castFromHand(player1, new SilvergillPeddler(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(torque.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's or differently typed permanent does not add a charge counter")
    void nonmatchingPermanentDoesNotAddChargeCounter() {
        Permanent torque = addTorque(CardSubtype.MERFOLK, 0);

        harness.castFromHand(player1, new SurlyFarrier(), "{1}{G}");
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new SilvergillPeddler(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(torque.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Removing three charge counters copies the next instant or sorcery")
    void copiesNextInstantOrSorcery() {
        Permanent torque = addTorque(CardSubtype.WIZARD, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(torque.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Copy Lightning Bolt"));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    private Permanent addTorque(CardSubtype chosenSubtype, int chargeCounters) {
        Permanent torque = harness.addToBattlefieldAndReturn(player1, new RimefireTorque());
        torque.setChosenSubtype(chosenSubtype);
        torque.setCounterCount(CounterType.CHARGE, chargeCounters);
        return torque;
    }

    @Test
    void subtypeChosenAsTorqueEntersControlsLaterTriggers() {
        harness.castFromHand(player1, new RimefireTorque(), "{1}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "MERFOLK");
        Permanent torque = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.castFromHand(player1, new SilvergillPeddler(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(torque.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void activationRequiresThreeChargeCounters() {
        Permanent torque = addTorque(CardSubtype.MERFOLK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(torque.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(torque.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyResolvesWithOriginalTargetsAndOnlyNextSpellIsCopied() {
        Permanent torque = addTorque(CardSubtype.MERFOLK, 4);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, null);
        assertThat(torque.isTapped()).isTrue();
        assertThat(torque.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyMayChooseANewTargetWithoutChangingOriginal() {
        addTorque(CardSubtype.MERFOLK, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({Divination.class})
    void creatureSpellDoesNotConsumeCopyAndNextSorceryIsCopied() {
        addTorque(CardSubtype.MERFOLK, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new SurlyFarrier(), "{1}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new SilvergillPeddler(), new SilvergillPeddler(),
                new SilvergillPeddler(), new SilvergillPeddler()));
        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }
}
