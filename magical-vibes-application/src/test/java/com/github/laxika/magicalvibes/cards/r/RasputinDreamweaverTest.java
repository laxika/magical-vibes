package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrownInIchor;
import com.github.laxika.magicalvibes.cards.p.Pyrotechnics;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RasputinDreamweaver.class, DrownInIchor.class, Pyrotechnics.class})
class RasputinDreamweaverTest extends BaseCardTest {

    @Test
    void entersWithSevenDreamCounters() {
        harness.castFromHand(player1, new RasputinDreamweaver(), "{4}{W}{U}");
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
    void cannotRemoveDreamCounterWhenNoneRemain() {
        addCreatureReady(player1, new RasputinDreamweaver());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters to remove");
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
    void damagePreventionAbilityPreventsOnlyTheNextDamageToRasputin() {
        Permanent rasputin = addCreatureReady(player1, new RasputinDreamweaver());
        rasputin.setCounterCount(CounterType.DREAM, 1);
        rasputin.setToughnessModifier(4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, Map.of(rasputin.getId(), 4));
        harness.passBothPriorities();

        assertThat(rasputin.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void addsDreamCounterAtUpkeepIfItStartedUntapped() {
        Permanent rasputin = addCreatureReady(player1, new RasputinDreamweaver());
        rasputin.setCounterCount(CounterType.DREAM, 0);

        advanceToUpkeep(player1);
        rasputin.tap();
        harness.passBothPriorities();

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
