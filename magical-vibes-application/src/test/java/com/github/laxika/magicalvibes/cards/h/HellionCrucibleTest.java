package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HellionCrucibleTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new HellionCrucible());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Second ability puts a pressure counter on the land")
    void putsPressureCounter() {
        Permanent crucible = harness.addToBattlefieldAndReturn(player1, new HellionCrucible());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(crucible.getCounterCount(CounterType.PRESSURE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Third ability removes two pressure counters, sacrifices the land, and makes a hasty 4/4 Hellion")
    void createsHellionToken() {
        Permanent crucible = harness.addToBattlefieldAndReturn(player1, new HellionCrucible());
        crucible.setCounterCount(CounterType.PRESSURE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hellion Crucible");
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Hellion".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Third ability cannot be activated without two pressure counters")
    void cannotActivateWithoutEnoughCounters() {
        Permanent crucible = harness.addToBattlefieldAndReturn(player1, new HellionCrucible());
        crucible.setCounterCount(CounterType.PRESSURE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
