package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NebelgastBeguiler.class, Plains.class})
class NebelgastBeguilerTest extends BaseCardTest {

    @Test
    @DisplayName("{W}, {T}: Tap target creature")
    void tapsTargetCreatureAndSource() {
        Permanent beguiler = addReadyBeguiler(player1);
        Permanent target = addCreatureReady(player2, new NebelgastBeguiler());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(beguiler.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNonCreatureTarget() {
        addReadyBeguiler(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        addReadyBeguiler(player1);
        Permanent target = addCreatureReady(player2, new NebelgastBeguiler());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void cannotActivateWithoutMana() {
        addReadyBeguiler(player1);
        Permanent target = addCreatureReady(player2, new NebelgastBeguiler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent beguiler = addReadyBeguiler(player1);
        beguiler.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, beguiler.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent beguiler = harness.addToBattlefieldAndReturn(player1, new NebelgastBeguiler());
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, beguiler.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItself() {
        Permanent beguiler = addReadyBeguiler(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, beguiler.getId());
        assertThat(beguiler.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(beguiler.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isFalse();
    }

    @Test
    void canTargetTappedCreatureAfterSourceLeaves() {
        Permanent beguiler = addReadyBeguiler(player1);
        Permanent target = addCreatureReady(player2, new NebelgastBeguiler());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(beguiler);
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isFalse();
    }

    @Test
    void tapsCreatureAfterSourceLeaves() {
        Permanent beguiler = addReadyBeguiler(player1);
        Permanent target = addCreatureReady(player2, new NebelgastBeguiler());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(target.isTapped()).isFalse();
        gd.playerBattlefields.get(player1.getId()).remove(beguiler);
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyBeguiler(Player player) {
        return addCreatureReady(player, new NebelgastBeguiler());
    }
}
