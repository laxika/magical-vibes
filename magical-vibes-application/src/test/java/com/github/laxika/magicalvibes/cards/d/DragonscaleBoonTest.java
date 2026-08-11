package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DragonscaleBoonTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on the target and untaps it")
    void addsCountersAndUntaps() {
        Permanent target = addTappedCreature(player1);
        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        Permanent target = addTappedCreature(player2);
        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addTappedCreature(player1);
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new DragonscaleBoon()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new DragonscaleBoon()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addTappedCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.tap();
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
