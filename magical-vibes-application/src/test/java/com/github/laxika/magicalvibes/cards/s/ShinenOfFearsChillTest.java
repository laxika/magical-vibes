package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GodosIrregulars;
import com.github.laxika.magicalvibes.cards.h.HandOfCruelty;
import com.github.laxika.magicalvibes.cards.m.MirenTheMoaningWell;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShinenOfFearsChill.class, HandOfCruelty.class, GodosIrregulars.class,
        MirenTheMoaningWell.class})
class ShinenOfFearsChillTest extends BaseCardTest {

    @Test
    @DisplayName("Shinen of Fear's Chill cannot block")
    void cannotBlock() {
        Permanent shinen = addCreatureReady(player2, new ShinenOfFearsChill());
        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());

        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(shinen);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Channel makes target creature unable to block this turn")
    void channelMakesTargetUnableToBlock() {
        harness.setHand(player1, List.of(new ShinenOfFearsChill()));
        Permanent target = addCreatureReady(player2, new HandOfCruelty());
        Permanent unaffected = addCreatureReady(player2, new HandOfCruelty());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlock(gd, target)).isFalse();
        assertThat(bls.canBlock(gd, unaffected)).isTrue();

        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(target);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shinen of Fear's Chill");
    }

    @Test
    @DisplayName("Channel's no-block effect wears off at end of turn")
    void channelCantBlockWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new ShinenOfFearsChill()));
        Permanent target = addCreatureReady(player2, new HandOfCruelty());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlock(gd, target)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bls.canBlock(gd, target)).isTrue();
    }

    @Test
    @DisplayName("Channel cannot target a noncreature permanent")
    void channelRejectsNoncreatureTarget() {
        harness.setHand(player1, List.of(new ShinenOfFearsChill()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MirenTheMoaningWell());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Shinen of Fear's Chill");
        assertThat(gd.stack).isEmpty();
    }
}
