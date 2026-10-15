package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Chillerpillar.class})
class ChillerpillarTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts two +1/+1 counters on Chillerpillar and gives it flying")
    void monstrosityMakesChillerpillarMonstrous() {
        Permanent chillerpillar = addReadyChillerpillar(player1);
        addMonstrosityMana(player1);

        assertThat(gqs.hasKeyword(gd, chillerpillar, Keyword.FLYING)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(chillerpillar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(chillerpillar.isMonstrous()).isTrue();
        assertThat(gqs.hasKeyword(gd, chillerpillar, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A monstrous Chillerpillar can activate monstrosity again without gaining counters")
    void monstrosityOnlyResolvesOnce() {
        Permanent chillerpillar = addReadyChillerpillar(player1);
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(chillerpillar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(chillerpillar.isMonstrous()).isTrue();
        assertThat(gqs.hasKeyword(gd, chillerpillar, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Two pending monstrosity activations only add counters once")
    void multiplePendingActivationsOnlyAddCountersOnce() {
        Permanent chillerpillar = addReadyChillerpillar(player1);
        addMonstrosityMana(player1);
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(chillerpillar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(chillerpillar.isMonstrous()).isTrue();
        assertThat(gqs.hasKeyword(gd, chillerpillar, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Monstrosity does not require tapping or being free of summoning sickness")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent chillerpillar = harness.addToBattlefieldAndReturn(player1, new Chillerpillar());
        chillerpillar.setSummoningSick(true);
        chillerpillar.tap();
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(chillerpillar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, chillerpillar, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("One snow mana is insufficient even with enough total mana")
    void requiresTwoSnowMana() {
        addReadyChillerpillar(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Nonsnow mana cannot pay Chillerpillar's snow activation cost")
    void requiresSnowMana() {
        addReadyChillerpillar(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyChillerpillar(Player player) {
        Permanent chillerpillar = harness.addToBattlefieldAndReturn(player, new Chillerpillar());
        chillerpillar.setSummoningSick(false);
        return chillerpillar;
    }

    private void addMonstrosityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 4);
        gd.playerManaPools.get(player.getId()).addSnowMana(ManaColor.BLUE, 2);
    }
}
