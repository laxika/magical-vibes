package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarkhideTroll.class, Shock.class})
class BarkhideTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Barkhide Troll enters with a +1/+1 counter")
    void entersWithPlusOneCounter() {
        harness.setHand(player1, List.of(new BarkhideTroll()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent troll = findPermanent(player1, "Barkhide Troll");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter grants hexproof until end of turn")
    void removesCounterAndGrantsHexproof() {
        Permanent troll = addTrollReady(player1);
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, troll, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Granted hexproof wears off at end of turn")
    void hexproofWearsOffAtEndOfTurn() {
        Permanent troll = addTrollReady(player1);
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, troll, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated without a +1/+1 counter")
    void cannotActivateWithoutPlusOneCounter() {
        addTrollReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterIsPaidBeforeHexproofResolves() {
        Permanent troll = addTrollReady(player1);
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.HEXPROOF)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent troll = addTrollReady(player1);
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, troll, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void cannotPayWithAnotherCounterType() {
        Permanent troll = addTrollReady(player1);
        troll.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(troll.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent troll = addTrollReady(player1);
        troll.setTapped(true);
        troll.setSummoningSick(true);
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, troll, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void hexproofInResponseMakesOpponentsSpellTargetIllegal() {
        Permanent troll = addTrollReady(player1);
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, troll.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, troll, Keyword.HEXPROOF)).isTrue();
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(troll);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(c -> c.getName()).containsExactly("Shock");
    }

    @Test
    void hexproofDoesNotPreventControllerFromTargetingTroll() {
        Permanent troll = addTrollReady(player1);
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, troll.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(troll);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName()).contains("Barkhide Troll", "Shock");
    }

    @Test
    void opponentCannotTargetAfterHexproofResolves() {
        Permanent troll = addTrollReady(player1);
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, troll.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(troll);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCanKillTrollBeforeHexproofResolves() {
        Permanent troll = addTrollReady(player1);
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player2, 0, troll.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(troll);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName()).containsExactly("Barkhide Troll");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void hexproofLastsThroughEndStepAndExpiresBeforeNextUpkeep() {
        Permanent troll = addTrollReady(player1);
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gqs.hasKeyword(gd, troll, Keyword.HEXPROOF)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, troll, Keyword.HEXPROOF)).isFalse();
    }

    private Permanent addTrollReady(Player player) {
        return addCreatureReady(player, new BarkhideTroll());
    }
}
