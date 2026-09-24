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
        Permanent planeswalker = new Permanent(new ChandraBoldPyromancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
