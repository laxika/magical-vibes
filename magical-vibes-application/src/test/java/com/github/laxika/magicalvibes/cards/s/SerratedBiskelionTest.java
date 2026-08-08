package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SerratedBiskelionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -1/-1 counter on itself and target creature")
    void putsCountersOnSourceAndTarget() {
        Permanent biskelion = addReadyBiskelion(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(biskelion.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(biskelion.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, biskelion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, biskelion)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadyBiskelion(player1);
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = findPermanent(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBiskelion(Player player) {
        Permanent perm = new Permanent(new SerratedBiskelion());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
