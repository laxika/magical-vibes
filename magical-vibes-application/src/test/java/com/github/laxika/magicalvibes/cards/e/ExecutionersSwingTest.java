package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExecutionersSwingTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a 2/2 that dealt combat damage this turn")
    void killsCreatureThatDealtCombatDamage() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        markDealtDamage(bears);

        castSwing(bears);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A surviving creature gets -5/-5 that wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent avatar = new Permanent(new AvatarOfMight());
        gd.playerBattlefields.get(player2.getId()).add(avatar);
        markDealtDamage(avatar);

        castSwing(avatar);

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(8);
    }

    @Test
    @DisplayName("Cannot target a creature that dealt no damage this turn")
    void cannotTargetCreatureThatDealtNoDamage() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that was only dealt damage itself")
    void cannotTargetCreatureThatOnlyTookDamage() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.permanentsDealtDamageThisTurn.add(bears.getId());

        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void markDealtDamage(Permanent permanent) {
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(permanent.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(player1.getId());
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new ExecutionersSwing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void castSwing(Permanent target) {
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
