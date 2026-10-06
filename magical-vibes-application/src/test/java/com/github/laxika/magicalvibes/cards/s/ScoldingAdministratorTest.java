package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScoldingAdministrator.class, Assassinate.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class ScoldingAdministratorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant that targets a creature puts a +1/+1 counter on the Administrator")
    void reparteeAddsCounterToSelf() {
        Permanent admin = addCreatureReady(player1, new ScoldingAdministrator());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveInstant(player1, 0, giantId);

        assertThat(admin.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        Permanent admin = addCreatureReady(player1, new ScoldingAdministrator());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(admin.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Dying with counters moves those counters to a chosen creature")
    void dyingWithCountersMovesThem() {
        Permanent admin = addCreatureReady(player1, new ScoldingAdministrator());
        admin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        admin.tap(); // so it can be Assassinated
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        UUID adminId = admin.getId();
        harness.castAndResolveSorcery(player2, 0, adminId);

        // Choose the Grizzly Bears to receive the counters
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve the counter-move trigger

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Dying with no counters does not trigger (intervening-if fails)")
    void dyingWithoutCountersDoesNotTrigger() {
        Permanent admin = addCreatureReady(player1, new ScoldingAdministrator());
        admin.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player2, 0, admin.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("A sorcery targeting your creature triggers Repartee")
    void sorceryTargetingOwnCreatureTriggers() {
        Permanent admin = addCreatureReady(player1, new ScoldingAdministrator());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(admin.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("An opponent's creature-targeting spell does not trigger Repartee")
    void opponentSpellDoesNotTrigger() {
        Permanent admin = addCreatureReady(player1, new ScoldingAdministrator());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, giant.getId());

        assertThat(admin.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Repartee resolves before a spell targeting the Administrator")
    void counterProtectsAdministratorFromShock() {
        Permanent admin = addCreatureReady(player1, new ScoldingAdministrator());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, admin.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(admin);
        assertThat(admin.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller may choose no target for the death trigger")
    void deathTriggerCanTargetNothing() {
        Permanent admin = addCreatureReady(player1, new ScoldingAdministrator());
        admin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        admin.tap();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player2, 0, admin.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(bears.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The death trigger puts every counter type on an opposing creature")
    void deathTriggerTransfersAllCounterTypesToOpponent() {
        Permanent admin = addCreatureReady(player1, new ScoldingAdministrator());
        admin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        admin.setCounterCount(CounterType.CHARGE, 3);
        admin.tap();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player2, 0, admin.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(3);
        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }
}
