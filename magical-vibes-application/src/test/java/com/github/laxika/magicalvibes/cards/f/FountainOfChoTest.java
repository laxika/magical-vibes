package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FountainOfChoTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new FountainOfCho()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Fountain of Cho").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping it puts a storage counter on it")
    void tappingItPutsStorageCounterOnIt() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfCho());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(fountain.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(fountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing storage counters adds that much white mana")
    void removingStorageCountersAddsWhiteMana() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfCho());
        fountain.setCounterCount(CounterType.STORAGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(fountain.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(fountain.isTapped()).isTrue();
    }
}
