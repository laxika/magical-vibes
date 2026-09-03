package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Whipcorder.class, GrizzlyBears.class, Forest.class})
class WhipcorderTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target creature")
    void tapsTargetCreature() {
        Permanent whipcorder = addReadyWhipcorder();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(whipcorder.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNoncreature() {
        addReadyWhipcorder();
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(forest);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        addReadyWhipcorder();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isFalse();
    }

    private Permanent addReadyWhipcorder() {
        Permanent whipcorder = new Permanent(new Whipcorder());
        whipcorder.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(whipcorder);
        return whipcorder;
    }
}
