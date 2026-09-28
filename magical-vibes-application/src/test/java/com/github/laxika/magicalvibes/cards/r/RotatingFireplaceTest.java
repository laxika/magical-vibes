package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RotatingFireplace.class)
class RotatingFireplaceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with a time counter")
    void entersTappedWithTimeCounter() {
        harness.setHand(player1, List.of(new RotatingFireplace()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent fireplace = findPermanent(player1, "Rotating Fireplace");
        assertThat(fireplace.isTapped()).isTrue();
        assertThat(fireplace.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds colorless mana equal to its time counters")
    void addsColorlessManaEqualToTimeCounters() {
        Permanent fireplace = harness.addToBattlefieldAndReturn(player1, new RotatingFireplace());
        fireplace.setCounterCount(CounterType.TIME, 3);
        fireplace.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(fireplace.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Time travels when its sorcery-speed ability resolves")
    void timeTravels() {
        Permanent fireplace = harness.addToBattlefieldAndReturn(player1, new RotatingFireplace());
        fireplace.setCounterCount(CounterType.TIME, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ADD");

        assertThat(fireplace.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }
}
