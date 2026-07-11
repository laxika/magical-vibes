package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImmaculateMagistrateTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one +1/+1 counter per Elf controlled on the target creature")
    void putsCounterPerElf() {
        // Magistrate is itself an Elf; add two Llanowar Elves → 3 Elves.
        addReadyCreature(player1, new ImmaculateMagistrate());
        addReadyCreature(player1, new LlanowarElves());
        addReadyCreature(player1, new LlanowarElves());
        Permanent bear = addReadyCreature(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counter count scales with the current number of Elves")
    void scalesWithElfCount() {
        // Only the Magistrate is an Elf → 1 counter.
        addReadyCreature(player1, new ImmaculateMagistrate());
        Permanent bear = addReadyCreature(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target any creature, including one the opponent controls")
    void canTargetOpponentCreature() {
        addReadyCreature(player1, new ImmaculateMagistrate());
        Permanent enemyBear = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, enemyBear.getId());
        harness.passBothPriorities();

        assertThat(enemyBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyCreature(player1, new ImmaculateMagistrate());
        Permanent land = addBasicLandForest(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addBasicLandForest(Player player) {
        Permanent perm = new Permanent(new com.github.laxika.magicalvibes.cards.f.Forest());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
