package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LightningHelix;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RamosDragonEngine.class, LightningHelix.class})
class RamosDragonEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a multicolored spell puts one counter on Ramos for each color")
    void castsSpellAndCountsItsColors() {
        Permanent ramos = harness.addToBattlefieldAndReturn(player1, new RamosDragonEngine());
        harness.setHand(player1, List.of(new LightningHelix()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(ramos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing five counters adds two mana of each color")
    void removesFiveCountersForTenMana() {
        Permanent ramos = harness.addToBattlefieldAndReturn(player1, new RamosDragonEngine());
        ramos.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        harness.activateAbility(player1, 0, null, null);

        assertThat(ramos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ramos's mana ability can be activated only once each turn")
    void manaAbilityIsLimitedToOncePerTurn() {
        Permanent ramos = harness.addToBattlefieldAndReturn(player1, new RamosDragonEngine());
        ramos.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);

        harness.activateAbility(player1, 0, null, null);
        ramos.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ramos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("A colorless spell triggers Ramos but adds no counters even when paid with colored mana")
    void colorlessSpellAddsNoCounters() {
        Permanent ramos = harness.addToBattlefieldAndReturn(player1, new RamosDragonEngine());
        harness.setHand(player1, List.of(new RamosDragonEngine()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(ramos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's spell does not trigger Ramos")
    void opponentSpellDoesNotAddCounters() {
        Permanent ramos = harness.addToBattlefieldAndReturn(player1, new RamosDragonEngine());
        harness.setHand(player2, List.of(new LightningHelix()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(ramos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Fewer than five counters cannot pay the mana ability's cost")
    void insufficientCountersCannotActivate() {
        Permanent ramos = harness.addToBattlefieldAndReturn(player1, new RamosDragonEngine());
        ramos.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ramos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        ramos.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.activateAbility(player1, 0, null, null);
        assertThat(ramos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(10);
    }

    @Test
    @DisplayName("The mana ability can be used again on the opponent's next turn")
    void activationLimitResetsOnNextTurn() {
        Permanent ramos = harness.addToBattlefieldAndReturn(player1, new RamosDragonEngine());
        ramos.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);
        harness.activateAbility(player1, 0, null, null);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.activateAbility(player1, 0, null, null);

        assertThat(ramos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(10);
        assertThat(gd.stack).isEmpty();
    }
}
