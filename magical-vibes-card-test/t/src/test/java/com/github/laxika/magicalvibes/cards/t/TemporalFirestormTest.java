package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemporalFirestorm.class, ChandraBoldPyromancer.class, GrizzlyBears.class})
class TemporalFirestormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to each creature and planeswalker when not kicked")
    void dealsDamageWithoutKicker() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownPlaneswalker = addChandra(player1, 8);
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingPlaneswalker = addChandra(player2, 8);

        castWithMana(List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingBear);
        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("One kicker allows phasing out one controlled creature or planeswalker")
    void oneKickerPhasesOutOneChosenPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent planeswalker = addChandra(player1, 8);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWithMana(List.of("{1}{W}"));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).contains(creature.getId(), planeswalker.getId())
                .doesNotContain(opposingCreature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(planeswalker.getId()));
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(planeswalker);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    @DisplayName("Both kicker options allow phasing out two controlled permanents")
    void bothKickersPhaseOutTwoChosenPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent planeswalker = addChandra(player1, 8);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWithMana(List.of("{1}{W}", "{1}{U}"));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).contains(creature.getId(), planeswalker.getId())
                .doesNotContain(opposingCreature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), planeswalker.getId()));
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature, planeswalker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    @DisplayName("Blue kicker protects a creature until its controller's next untap")
    void blueKickerProtectsCreatureUntilNextUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWithMana(List.of("{1}{U}"));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.performUntapStep(player2);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("A kicked spell may phase out no permanents and still deal damage")
    void mayChooseNoPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent planeswalker = addChandra(player1, 8);

        castWithMana(List.of("{1}{W}"));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(planeswalker);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Both kickers permit choosing only one permanent")
    void mayChooseFewerThanNumberOfKicks() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent planeswalker = addChandra(player1, 8);

        castWithMana(List.of("{1}{W}", "{1}{U}"));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(planeswalker);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Kicked spell resolves with no eligible controlled permanents")
    void resolvesWithoutEligibleControlledPermanents() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingPlaneswalker = addChandra(player2, 5);

        castWithMana(List.of("{1}{W}", "{1}{U}"));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(opposingCreature, opposingPlaneswalker);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Chandra, Bold Pyromancer");
        harness.assertInGraveyard(player1, "Temporal Firestorm");
    }

    @Test
    @DisplayName("The white kicker cannot be paid twice")
    void cannotPayWhiteKickerTwice() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> castWithMana(List.of("{1}{W}", "{1}{W}")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("may be paid at most 1");
    }

    @Test
    @DisplayName("The blue kicker cannot be paid twice")
    void cannotPayBlueKickerTwice() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> castWithMana(List.of("{1}{U}", "{1}{U}")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("may be paid at most 1");
    }

    private void castWithMana(List<String> kickerPayments) {
        harness.setHand(player1, List.of(new TemporalFirestorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3 + kickerPayments.size());
        if (kickerPayments.contains("{1}{W}")) {
            harness.addMana(player1, ManaColor.WHITE, 1);
        }
        if (kickerPayments.contains("{1}{U}")) {
            harness.addMana(player1, ManaColor.BLUE, 1);
        }
        harness.castSorceryWithRepeatedCosts(player1, 0, kickerPayments, List.of());
    }

    private Permanent addChandra(Player player, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new ChandraBoldPyromancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        return planeswalker;
    }
}
