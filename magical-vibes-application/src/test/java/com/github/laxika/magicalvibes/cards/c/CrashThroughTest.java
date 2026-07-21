package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrashThroughTest extends BaseCardTest {

    @Test
    @DisplayName("Crash Through grants trample to own creatures and draws a card")
    void grantsTrampleAndDraws() {
        Permanent own = addReadyCreature(player1, new GrizzlyBears());
        Permanent enemy = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrashThrough()));
        harness.addMana(player1, ManaColor.RED, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(own.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(enemy.hasKeyword(Keyword.TRAMPLE)).isFalse();
        // Started at handBefore, cast one card (-1), drew one (+1) => back to handBefore.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Crash Through trample wears off at end of turn")
    void trampleWearsOff() {
        Permanent own = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrashThrough()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(own.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(own.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
