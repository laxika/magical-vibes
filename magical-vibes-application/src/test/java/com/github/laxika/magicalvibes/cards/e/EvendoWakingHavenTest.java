package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({EvendoWakingHaven.class, GrizzlyBears.class})
class EvendoWakingHavenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new EvendoWakingHaven()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Evendo, Waking Haven").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one green mana")
    void tapAbilityAddsGreenMana() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        haven.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Station adds charge counters equal to another creature's power")
    void stationUsesAnotherCreaturePower() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(haven.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Twelve charge counters unlock the creature-scaled mana ability")
    void twelveChargeCountersAddManaForEachCreature() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        haven.setCounterCount(CounterType.CHARGE, 12);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature-scaled mana ability requires twelve charge counters")
    void creatureScaledManaAbilityRequiresTwelveChargeCounters() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        haven.setCounterCount(CounterType.CHARGE, 11);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("charge counters");
    }

}
