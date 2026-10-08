package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FaerieGuidemother;
import com.github.laxika.magicalvibes.cards.f.FlaxenIntruder;
import com.github.laxika.magicalvibes.cards.g.GarenbrigSquire;
import com.github.laxika.magicalvibes.cards.g.GiftOfTheFae;
import com.github.laxika.magicalvibes.cards.m.MaraleafRider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildbornPreserver.class, MaraleafRider.class, GarenbrigSquire.class,
        FaerieGuidemother.class, GiftOfTheFae.class, FlaxenIntruder.class, WelcomeHome.class})
class WildbornPreserverTest extends BaseCardTest {

    @Test
    @DisplayName("A non-Human creature entering lets its controller pay X for counters")
    void nonHumanCreatureEnteringAddsCountersAfterPayment() {
        harness.addToBattlefield(player1, new WildbornPreserver());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromHand(player1, new MaraleafRider(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        Permanent preserver = findPermanent(player1, "Wildborn Preserver");
        assertThat(preserver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Choosing X=0 does not add counters")
    void choosingZeroDoesNothing() {
        harness.addToBattlefield(player1, new WildbornPreserver());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromHand(player1, new MaraleafRider(), "{1}{G}");
        resolveAllTriggers();
        harness.handleXValueChosen(player1, 0);

        Permanent preserver = findPermanent(player1, "Wildborn Preserver");
        assertThat(preserver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Human creatures do not trigger Wildborn Preserver")
    void humanCreatureEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new WildbornPreserver());
        harness.castFromHand(player1, new GarenbrigSquire(), "{1}{G}");
        harness.passBothPriorities();

        Permanent preserver = findPermanent(player1, "Wildborn Preserver");
        assertThat(preserver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying X creates a separate trigger before counters are added")
    void paymentLeavesCountersPendingUntilSeparateTriggerResolves() {
        Permanent preserver = harness.addToBattlefieldAndReturn(player1, new WildbornPreserver());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromHand(player1, new MaraleafRider(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(preserver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(preserver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Wildborn Preserver does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromHand(player1, new WildbornPreserver(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Wildborn Preserver")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's non-Human creature does not trigger Wildborn Preserver")
    void opponentsCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new WildbornPreserver());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new MaraleafRider(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Wildborn Preserver")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Without available mana the entry trigger resolves without counters")
    void noManaDoesNotAddCounters() {
        harness.addToBattlefield(player1, new WildbornPreserver());
        harness.castFromHand(player1, new MaraleafRider(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Wildborn Preserver")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each non-Human token entering simultaneously triggers separately")
    void simultaneousTokensEachAllowIndependentPayment() {
        Permanent preserver = harness.addToBattlefieldAndReturn(player1, new WildbornPreserver());
        harness.setHand(player1, List.of(new FlaxenIntruder()));
        harness.addMana(player1, ManaColor.GREEN, 10);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
            harness.handleXValueChosen(player1, 1);
            harness.passBothPriorities();
        }

        assertThat(preserver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash allows Wildborn Preserver to be cast during an opponent's combat")
    void canBeCastDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castFromHand(player1, new WildbornPreserver(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wildborn Preserver");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reach allows Wildborn Preserver to block a flying creature")
    void canBlockFlyingCreature() {
        addCreatureReady(player1, new FaerieGuidemother());
        addCreatureReady(player2, new WildbornPreserver());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Faerie Guidemother");
        harness.assertOnBattlefield(player2, "Wildborn Preserver");
    }
}
