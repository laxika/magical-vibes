package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.v.VanishingVerse;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpitefulSquad.class, Assassinate.class, GrizzlyBears.class, HillGiant.class, VanishingVerse.class})
class SpitefulSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two +1/+1 counters")
    void entersWithTwoPlusOneCounters() {
        harness.castFromHand(player1, new SpitefulSquad(), "{2}{W}{B}");
        harness.passBothPriorities();

        Permanent squad = findPermanent(player1, "Spiteful Squad");
        assertThat(squad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, squad)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, squad)).isEqualTo(2);
    }

    @Test
    @DisplayName("On death, puts all its counters on a creature you control")
    void deathTriggerPutsCountersOnControlledCreature() {
        Permanent squad = addCreatureReady(player1, new SpitefulSquad());
        squad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        squad.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        squad.tap();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        killSquad(squad);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The death trigger is not put on the stack without a controlled creature target")
    void deathTriggerNeedsControlledCreatureTarget() {
        Permanent squad = addCreatureReady(player1, new SpitefulSquad());
        squad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        squad.tap();

        killSquad(squad);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }

    @Test
    @DisplayName("Death with no counters still requires a creature target")
    void deathWithoutCountersStillTriggers() {
        addCreatureReady(player1, new SpitefulSquad());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spiteful Squad");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(recipient.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The death trigger adds each counter kind to existing counters")
    void transfersNonPowerToughnessCountersAsWell() {
        Permanent squad = addCreatureReady(player1, new SpitefulSquad());
        squad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        squad.setCounterCount(CounterType.CHARGE, 3);
        squad.setCounterCount(CounterType.FLYING, 1);
        squad.tap();
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        recipient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        recipient.setCounterCount(CounterType.CHARGE, 2);

        killSquad(squad);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(recipient.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
        assertThat(recipient.getCounterCount(CounterType.FLYING)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters that cause zero toughness are included in the death trigger")
    void deathFromMinusCountersTransfersTheirExcess() {
        Permanent squad = addCreatureReady(player1, new SpitefulSquad());
        squad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        squad.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spiteful Squad");
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(recipient.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, recipient)).isEqualTo(2);
    }

    @Test
    @DisplayName("The death trigger does nothing if its target leaves the battlefield")
    void deathTriggerDoesNotRetargetWhenTargetIsExiled() {
        Permanent squad = addCreatureReady(player1, new SpitefulSquad());
        squad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        squad.tap();
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        killSquad(squad);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.setHand(player2, List.of(new VanishingVerse()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, recipient.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void killSquad(Permanent squad) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player2, 0, squad.getId());
    }
}
