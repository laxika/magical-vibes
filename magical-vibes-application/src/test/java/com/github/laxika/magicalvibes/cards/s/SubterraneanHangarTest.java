package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SubterraneanHangarTest extends BaseCardTest {

    @Test
    @DisplayName("Subterranean Hangar enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new SubterraneanHangar()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findPermanent(player1, "Subterranean Hangar").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The first ability puts a storage counter on the land")
    void storesACounter() {
        Permanent hangar = addHangarWithCounters(0);
        hangar.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(hangar.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(hangar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing all storage counters adds that much black mana")
    void removingAllCountersAddsBlack() {
        Permanent hangar = addHangarWithCounters(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "3");

        assertThat(blackMana()).isEqualTo(3);
        assertThat(hangar.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(hangar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing fewer counters than present keeps the rest")
    void removingSomeCountersKeepsTheRest() {
        Permanent hangar = addHangarWithCounters(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "1");

        assertThat(blackMana()).isEqualTo(1);
        assertThat(hangar.getCounterCount(CounterType.STORAGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing zero counters produces no mana but still taps the land")
    void removingZeroCountersProducesNoMana() {
        Permanent hangar = addHangarWithCounters(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(blackMana()).isZero();
        assertThat(hangar.getCounterCount(CounterType.STORAGE)).isEqualTo(3);
        assertThat(hangar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating with no storage counters produces no mana and no choice")
    void activatingWithNoCountersProducesNoMana() {
        Permanent hangar = addHangarWithCounters(0);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(blackMana()).isZero();
        assertThat(hangar.isTapped()).isTrue();
    }

    private Permanent addHangarWithCounters(int counters) {
        Permanent hangar = harness.addToBattlefieldAndReturn(player1, new SubterraneanHangar());
        hangar.setSummoningSick(false);
        if (counters > 0) {
            hangar.setCounterCount(CounterType.STORAGE, counters);
        }
        return hangar;
    }

    private int blackMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK);
    }
}
