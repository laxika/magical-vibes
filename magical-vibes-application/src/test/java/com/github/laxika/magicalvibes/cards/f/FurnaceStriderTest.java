package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({FurnaceStrider.class})
class FurnaceStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two oil counters")
    void entersWithTwoOilCounters() {
        harness.setHand(player1, List.of(new FurnaceStrider()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent strider = findPermanent(player1, "Furnace Strider");
        assertThat(strider.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes an oil counter to grant haste to a creature you control")
    void removesOilCounterToGrantHaste() {
        Permanent strider = addCreatureReady(player1, new FurnaceStrider());
        strider.setCounterCount(CounterType.OIL, 2);
        Permanent target = addCreatureReady(player1, new FurnaceStrider());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(strider.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void grantedHasteWearsOffAtEndOfTurn() {
        Permanent strider = addCreatureReady(player1, new FurnaceStrider());
        strider.setCounterCount(CounterType.OIL, 1);
        Permanent target = addCreatureReady(player1, new FurnaceStrider());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without an oil counter")
    void cannotActivateWithoutOilCounter() {
        addCreatureReady(player1, new FurnaceStrider());
        Permanent target = addCreatureReady(player1, new FurnaceStrider());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent strider = addCreatureReady(player1, new FurnaceStrider());
        strider.setCounterCount(CounterType.OIL, 1);
        Permanent target = addCreatureReady(player2, new FurnaceStrider());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(strider.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target itself while summoning sick and tapped, paying the last oil counter immediately")
    void canGrantItselfHasteWithLastCounter() {
        Permanent strider = harness.enterBattlefieldAndReturn(player1, new FurnaceStrider());
        strider.setCounterCount(CounterType.OIL, 1);
        strider.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThat(strider.isSummoningSick()).isTrue();
        harness.activateAbility(player1, 0, null, strider.getId());

        assertThat(strider.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gqs.hasKeyword(gd, strider, Keyword.HASTE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, strider, Keyword.HASTE)).isTrue();
        assertThat(strider.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, strider.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Other counter types cannot pay the oil counter cost")
    void cannotSpendOtherCounterTypes() {
        Permanent strider = addCreatureReady(player1, new FurnaceStrider());
        strider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, strider.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(strider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that changes controllers before resolution does not gain haste")
    void targetMustStillBeControlledAtResolution() {
        Permanent strider = harness.enterBattlefieldAndReturn(player1, new FurnaceStrider());
        Permanent target = addCreatureReady(player1, new FurnaceStrider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(strider.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability resolves even if Furnace Strider leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent strider = harness.enterBattlefieldAndReturn(player1, new FurnaceStrider());
        Permanent target = addCreatureReady(player1, new FurnaceStrider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(strider);
        gd.playerGraveyards.get(player1.getId()).add(strider.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }
}
