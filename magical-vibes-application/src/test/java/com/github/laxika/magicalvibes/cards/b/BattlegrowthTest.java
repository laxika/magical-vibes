package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
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

@CardUsed({Battlegrowth.class, AlphaMyr.class, Bonesplitter.class})
class BattlegrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Battlegrowth puts a +1/+1 counter on target creature")
    void putsCounterOnTargetCreature() {
        Permanent target = addCreatureReady(player2, new AlphaMyr());

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Battlegrowth can target a creature you control")
    void canTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new AlphaMyr());

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Battlegrowth cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        harness.setHand(player1, List.of(new Battlegrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, equipment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Battlegrowth does nothing if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new AlphaMyr());
        harness.setHand(player1, List.of(new Battlegrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new Battlegrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
