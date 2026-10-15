package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MeliraSylvokOutcast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulImmolation.class, ColossalDreadmaw.class, HillGiant.class, MeliraSylvokOutcast.class})
class SoulImmolationTest extends BaseCardTest {

    @Test
    void blightsChosenCreatureAndDamagesEachOpponentAndTheirCreatures() {
        Permanent blightCreature = addCreatureReady(player1, new ColossalDreadmaw());
        addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new SoulImmolation()));
        harness.addMana(player1, ManaColor.RED, 5);

        gs.playCard(gd, player1, 0, 3, null, null, List.of(), List.of(), false, blightCreature.getId());

        assertThat(blightCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void cannotAnnounceMoreThanGreatestControlledToughness() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new SoulImmolation()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 4, null, null,
                List.of(), List.of(), false, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X can't be greater than 3");

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void canBlightSmallerCreatureForXDeterminedByAnotherCreature() {
        Permanent largeCreature = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent blightedCreature = addCreatureReady(player1, new HillGiant());
        Permanent opposingCreature = addCreatureReady(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new SoulImmolation()));
        harness.addMana(player1, ManaColor.RED, 5);

        gs.playCard(gd, player1, 0, 4, null, null, List.of(), List.of(), false, blightedCreature.getId());

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        assertThat(largeCreature.getMarkedDamage()).isZero();
        assertThat(largeCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
    }

    @Test
    void zeroXPlacesNoCountersAndDealsNoDamage() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        Permanent opposingCreature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new SoulImmolation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorceryWithSacrifice(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(opposingCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Soul Immolation");
    }

    @Test
    void cannotBlightAnOpponentsCreatureToPayCost() {
        addCreatureReady(player1, new HillGiant());
        Permanent opposingCreature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new SoulImmolation()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null,
                List.of(), List.of(), false, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(opposingCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertInHand(player1, "Soul Immolation");
    }

    @Test
    void cannotPayPositiveBlightCostWhenMinusCountersAreProhibited() {
        Permanent melira = addCreatureReady(player1, new MeliraSylvokOutcast());
        harness.setHand(player1, List.of(new SoulImmolation()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null,
                List.of(), List.of(), false, melira.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(melira.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertInHand(player1, "Soul Immolation");
    }
}
