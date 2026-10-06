package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
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

@CardUsed({SigilOfDistinction.class, CylianElf.class})
class SigilOfDistinctionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sigil of Distinction with X=3 enters with 3 charge counters")
    void entersWith3ChargeCounters() {
        harness.setHand(player1, List.of(new SigilOfDistinction()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent sigil = findPermanent(player1, "Sigil of Distinction");
        assertThat(sigil).isNotNull();
        assertThat(sigil.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each charge counter")
    void equippedCreatureBoostedPerChargeCounter() {
        Permanent bears = addCreatureReady(player1, new CylianElf());
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfDistinction());
        sigil.setCounterCount(CounterType.CHARGE, 3);
        sigil.setAttachedTo(bears.getId());

        // 2/2 base + 3 charge counters = 5/5
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equipped creature gets no boost with zero charge counters")
    void noBoostWithZeroChargeCounters() {
        Permanent bears = addCreatureReady(player1, new CylianElf());
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfDistinction());
        sigil.setCounterCount(CounterType.CHARGE, 0);
        sigil.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipping removes a charge counter and attaches to the target")
    void equipRemovesChargeCounterAndAttaches() {
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfDistinction());
        sigil.setCounterCount(CounterType.CHARGE, 3);
        Permanent bears = addCreatureReady(player1, new CylianElf());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(sigil.getAttachedTo()).isEqualTo(bears.getId());
        // One charge counter spent to equip: 3 -> 2
        assertThat(sigil.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        // 2/2 base + 2 remaining charge counters = 4/4
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot equip with no charge counters to remove")
    void cannotEquipWithoutChargeCounters() {
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfDistinction());
        sigil.setCounterCount(CounterType.CHARGE, 0);
        Permanent bears = addCreatureReady(player1, new CylianElf());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot equip a creature an opponent controls")
    void cannotEquipOpponentCreature() {
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfDistinction());
        sigil.setCounterCount(CounterType.CHARGE, 3);

        Permanent opponentBears = addCreatureReady(player2, new CylianElf());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castingWithZeroXEntersWithoutCounters() {
        harness.setHand(player1, List.of(new SigilOfDistinction()));

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sigil of Distinction")
                .getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void enteringWithoutBeingCastHasZeroChargeCounters() {
        Permanent sigil = harness.enterBattlefieldAndReturn(player1, new SigilOfDistinction());

        assertThat(sigil.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void spendingLastCounterStillEquipsAndImmediatelyRemovesBoost() {
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfDistinction());
        sigil.setCounterCount(CounterType.CHARGE, 1);
        Permanent first = addCreatureReady(player1, new CylianElf());
        Permanent second = addCreatureReady(player1, new CylianElf());
        sigil.setAttachedTo(first.getId());

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(sigil.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(sigil.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(sigil.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void disappearingEquipTargetDoesNotRefundCounterOrDetachEquipment() {
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfDistinction());
        sigil.setCounterCount(CounterType.CHARGE, 3);
        Permanent first = addCreatureReady(player1, new CylianElf());
        Permanent second = addCreatureReady(player1, new CylianElf());
        sigil.setAttachedTo(first.getId());

        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerGraveyards.get(player1.getId()).add(second.getCard());
        harness.passBothPriorities();

        assertThat(sigil.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(sigil.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
    }

    @Test
    void equipCannotBeActivatedOutsideMainPhase() {
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfDistinction());
        sigil.setCounterCount(CounterType.CHARGE, 3);
        Permanent creature = addCreatureReady(player1, new CylianElf());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sigil.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(sigil.getAttachedTo()).isNull();
    }
}
