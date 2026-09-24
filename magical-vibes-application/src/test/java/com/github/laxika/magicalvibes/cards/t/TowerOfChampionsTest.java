package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TowerOfChampions.class, AlphaMyr.class, Forest.class})
class TowerOfChampionsTest extends BaseCardTest {

    @Test
    @DisplayName("The ability gives a target creature +6/+6 until end of turn")
    void boostsTargetCreatureUntilEndOfTurn() {
        harness.addToBattlefield(player1, new TowerOfChampions());
        Permanent target = addCreatureReady(player2, new AlphaMyr());
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 6);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Activating the ability taps the Tower and pays its eight-mana cost")
    void activationTapsTowerAndConsumesMana() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfChampions());
        Permanent target = addCreatureReady(player2, new AlphaMyr());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(tower.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The ability cannot be activated without eight mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfChampions());
        Permanent target = addCreatureReady(player2, new AlphaMyr());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(tower.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability fizzles if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new TowerOfChampions());
        Permanent target = addCreatureReady(player2, new AlphaMyr());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfChampions());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tower.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(8);
        assertThat(gd.stack).isEmpty();
    }
}
