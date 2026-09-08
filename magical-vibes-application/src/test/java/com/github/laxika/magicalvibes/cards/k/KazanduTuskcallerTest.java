package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KazanduTuskcallerTest extends BaseCardTest {

    @Test
    @DisplayName("At levels two through five Kazandu Tuskcaller creates one Elephant token")
    void createsOneElephantAtLevelsTwoThroughFive() {
        Permanent tuskcaller = addCreatureReady(player1, new KazanduTuskcaller());
        tuskcaller.setCounterCount(CounterType.LEVEL, 2);
        prepareForAbility(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elephant")).isEqualTo(1);
        Permanent elephant = findPermanent(player1, "Elephant");
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
    }

    @Test
    @DisplayName("At level six Kazandu Tuskcaller creates two Elephant tokens")
    void createsTwoElephantsAtLevelSix() {
        Permanent tuskcaller = addCreatureReady(player1, new KazanduTuskcaller());
        tuskcaller.setCounterCount(CounterType.LEVEL, 6);
        prepareForAbility(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elephant")).isEqualTo(2);
    }

    @Test
    @DisplayName("Kazandu Tuskcaller has no token ability before level two")
    void hasNoTokenAbilityBeforeLevelTwo() {
        addCreatureReady(player1, new KazanduTuskcaller());
        prepareForAbility(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareForAbility(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
