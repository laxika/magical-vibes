package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PredatorsStrike;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikeshotGoblin.class, PredatorsStrike.class, Terror.class})
class SpikeshotGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to its power to a target player")
    void dealsPowerDamageToTargetPlayer() {
        Permanent goblin = addCreatureReady(player1, new SpikeshotGoblin());
        goblin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals damage equal to its power to a target creature")
    void dealsPowerDamageToTargetCreature() {
        Permanent goblin = addCreatureReady(player1, new SpikeshotGoblin());
        goblin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new SpikeshotGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Spikeshot Goblin"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spikeshot Goblin");
        harness.assertInGraveyard(player2, "Spikeshot Goblin");
    }

    @Test
    @DisplayName("Taps as part of activating the ability")
    void tapsAsActivationCost() {
        Permanent goblin = addCreatureReady(player1, new SpikeshotGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(goblin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new SpikeshotGoblin());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new SpikeshotGoblin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Uses power after a pump spell resolves in response")
    void usesPowerAtResolution() {
        Permanent goblin = addCreatureReady(player1, new SpikeshotGoblin());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PredatorsStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.castAndResolveInstant(player1, 0, goblin.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Uses last known power including a pump received after activation")
    void usesLastKnownPowerAfterSourceDies() {
        Permanent goblin = addCreatureReady(player1, new SpikeshotGoblin());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PredatorsStrike(), new Terror()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.castAndResolveInstant(player1, 0, goblin.getId());
        harness.castAndResolveInstant(player1, 0, goblin.getId());
        harness.assertInGraveyard(player1, "Spikeshot Goblin");
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Deals no damage when its power is zero")
    void zeroPowerDealsNoDamage() {
        Permanent goblin = addCreatureReady(player1, new SpikeshotGoblin());
        goblin.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(goblin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent goblin = addCreatureReady(player1, new SpikeshotGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spikeshot Goblin");
        assertThat(goblin.getMarkedDamage()).isEqualTo(1);
    }
}
