package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.o.OvergrownZealot;
import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShrewdStoryteller.class, OvergrownZealot.class, Murder.class, RelentlessAssault.class})
class ShrewdStorytellerTest extends BaseCardTest {

    @Test
    void tappedStorytellerPutsCounterOnTargetCreatureAtPostcombatMain() {
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OvergrownZealot());
        storyteller.tap();

        advanceToPostcombatMain(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void untappedStorytellerDoesNotTrigger() {
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OvergrownZealot());

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(storyteller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void untappingBeforeResolutionPreventsCounter() {
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OvergrownZealot());
        storyteller.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, target.getId());
        storyteller.untap();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void survivalTriggersAgainOnALaterTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OvergrownZealot());
        storyteller.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        storyteller.tap();
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canTargetItself() {
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        storyteller.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, storyteller.getId());
        harness.passBothPriorities();

        assertThat(storyteller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerOnOpponentsSecondMainPhase() {
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        storyteller.tap();

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(storyteller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void survivalDoesNotTriggerInThirdMainPhase() {
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OvergrownZealot());
        storyteller.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new RelentlessAssault()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.passUntilWithNoAttackers(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void tappingAfterSecondMainBeginsDoesNotEnableThirdMainTrigger() {
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        advanceToPostcombatMain(player1);
        storyteller.tap();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.setHand(player1, List.of(new RelentlessAssault()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.passUntilWithNoAttackers(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(storyteller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void tappedSourceLeavingBattlefieldStillAllowsCounter() {
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OvergrownZealot());
        storyteller.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, storyteller.getId());
        harness.assertNotOnBattlefield(player1, "Shrewd Storyteller");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void untappedSourceLeavingBattlefieldPreventsCounter() {
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OvergrownZealot());
        storyteller.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, target.getId());
        storyteller.untap();
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, storyteller.getId());
        harness.assertNotOnBattlefield(player1, "Shrewd Storyteller");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
