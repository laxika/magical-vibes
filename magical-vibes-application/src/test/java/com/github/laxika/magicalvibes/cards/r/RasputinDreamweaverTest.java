package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrownInIchor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RasputinDreamweaver.class, DrownInIchor.class})
class RasputinDreamweaverTest extends BaseCardTest {

    @Test
    void entersWithSevenDreamCounters() {
        harness.setHand(player1, List.of(new RasputinDreamweaver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Rasputin Dreamweaver")
                .getCounterCount(CounterType.DREAM)).isEqualTo(7);
    }

    @Test
    void removingDreamCounterAddsColorlessMana() {
        Permanent rasputin = addCreatureReady(player1, new RasputinDreamweaver());
        rasputin.setCounterCount(CounterType.DREAM, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void removingDreamCounterCreatesDamagePreventionShield() {
        Permanent rasputin = addCreatureReady(player1, new RasputinDreamweaver());
        rasputin.setCounterCount(CounterType.DREAM, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isZero();
        assertThat(rasputin.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    void addsDreamCounterAtUpkeepIfItStartedUntapped() {
        Permanent rasputin = addCreatureReady(player1, new RasputinDreamweaver());
        rasputin.setCounterCount(CounterType.DREAM, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1");

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isEqualTo(1);
    }

    @Test
    void doesNotAddDreamCounterIfItStartedTapped() {
        Permanent rasputin = addCreatureReady(player1, new RasputinDreamweaver());
        rasputin.setCounterCount(CounterType.DREAM, 0);
        rasputin.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isZero();
    }

    @Test
    void upkeepCounterCannotExceedSeven() {
        Permanent rasputin = addCreatureReady(player1, new RasputinDreamweaver());
        rasputin.setCounterCount(CounterType.DREAM, 7);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isEqualTo(7);
    }

    @Test
    void staticLimitAlsoAppliesToProliferate() {
        Permanent rasputin = addCreatureReady(player1, new RasputinDreamweaver());
        rasputin.setCounterCount(CounterType.DREAM, 7);
        rasputin.setToughnessModifier(4);

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DrownInIchor()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, rasputin.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(rasputin.getId()));

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isEqualTo(7);
    }
}
