package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArlinnVoiceOfThePack;
import com.github.laxika.magicalvibes.cards.c.CruelCelebrant;
import com.github.laxika.magicalvibes.cards.p.PollenbrightDruid;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevouringHellion.class, ArlinnVoiceOfThePack.class, CruelCelebrant.class,
        PollenbrightDruid.class, Mountain.class})
class DevouringHellionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature and planeswalker gives two counters per permanent")
    void sacrificingCreatureAndPlaneswalkerAddsTwoCountersEach() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());
        Permanent arlinn = harness.addToBattlefieldAndReturn(player1, new ArlinnVoiceOfThePack());
        arlinn.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefieldAndReturn(player1, new Mountain());

        castHellion();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(druid.getId(), arlinn.getId()));

        assertThat(findPermanent(player1, "Devouring Hellion")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(countPermanents(player1, "Pollenbright Druid")).isZero();
        assertThat(countPermanents(player1, "Arlinn, Voice of the Pack")).isZero();
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing zero permanents leaves the Hellion without counters")
    void choosingZeroPermanentsLeavesItWithoutCounters() {
        harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());

        castHellion();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(findPermanent(player1, "Devouring Hellion")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Pollenbright Druid")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not prompt or sacrifice a noncreature nonplaneswalker")
    void doesNotSacrificeNonmatchingPermanent() {
        harness.addToBattlefieldAndReturn(player1, new Mountain());

        castHellion();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Devouring Hellion")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(1);
    }

    @Test
    @DisplayName("Selected creatures die simultaneously even when the death-trigger source is selected first")
    void sacrificedCelebrantSeesAllSelectedDeaths() {
        Permanent celebrant = harness.addToBattlefieldAndReturn(player1, new CruelCelebrant());
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castHellion();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(celebrant.getId(), druid.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(findPermanent(player1, "Devouring Hellion")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Cruel Celebrant");
        harness.assertNotOnBattlefield(player1, "Pollenbright Druid");
    }

    @Test
    @DisplayName("Only other eligible permanents controlled by the Hellion's controller can be sacrificed")
    void choiceExcludesHellionOpponentsAndLands() {
        Permanent ownDruid = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());
        harness.addToBattlefield(player2, new PollenbrightDruid());
        harness.addToBattlefield(player1, new Mountain());

        castHellion();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                (PendingInteraction.MultiPermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(ownDruid.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(ownDruid.getId()));

        assertThat(findPermanent(player1, "Devouring Hellion")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Pollenbright Druid");
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("A planeswalker alone can be sacrificed for two counters")
    void sacrificesPlaneswalkerWithoutAnyOtherCreatures() {
        Permanent arlinn = harness.addToBattlefieldAndReturn(player1, new ArlinnVoiceOfThePack());
        arlinn.setCounterCount(CounterType.LOYALTY, 7);

        castHellion();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(arlinn.getId()));

        assertThat(findPermanent(player1, "Devouring Hellion")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Arlinn, Voice of the Pack");
    }

    private void castHellion() {
        harness.castFromHand(player1, new DevouringHellion(), "{2}{R}");
    }
}
