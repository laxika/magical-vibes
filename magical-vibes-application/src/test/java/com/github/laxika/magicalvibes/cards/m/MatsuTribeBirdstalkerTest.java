package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MatsuTribeBirdstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksCreature() {
        Permanent birdstalker = addBirdstalkerReady(player1);
        birdstalker.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        for (int guard = 0; guard < 40 && !gd.stack.isEmpty() && !gd.interaction.isAwaitingInput(); guard++) {
            harness.passBothPriorities();
        }

        Permanent spider = findPermanent(player2, "Giant Spider");
        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to a player does not tap or lock a creature")
    void unblockedDamageDoesNotTapCreature() {
        Permanent birdstalker = addBirdstalkerReady(player1);
        birdstalker.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        for (int guard = 0; guard < 40 && !gd.stack.isEmpty() && !gd.interaction.isAwaitingInput(); guard++) {
            harness.passBothPriorities();
        }

        Permanent spider = findPermanent(player2, "Giant Spider");
        assertThat(spider.isTapped()).isFalse();
        assertThat(spider.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Green activation grants reach until end of turn")
    void activationGrantsReachUntilEndOfTurn() {
        Permanent birdstalker = addBirdstalkerReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, birdstalker, Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, birdstalker, Keyword.REACH)).isFalse();
    }

    private Permanent addBirdstalkerReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new MatsuTribeBirdstalker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
