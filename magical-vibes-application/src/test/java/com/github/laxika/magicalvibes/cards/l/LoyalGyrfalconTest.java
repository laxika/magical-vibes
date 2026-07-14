package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoyalGyrfalconTest extends BaseCardTest {

    @BeforeEach
    void setUpTest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting a white spell makes the Gyrfalcon lose defender")
    void whiteSpellRemovesDefender() {
        Permanent falcon = addCreatureReady(player1, new LoyalGyrfalcon());
        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isTrue();

        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Casting a non-white spell leaves defender in place")
    void nonWhiteSpellKeepsDefender() {
        Permanent falcon = addCreatureReady(player1, new LoyalGyrfalcon());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Defender returns at end of turn")
    void defenderReturnsAtEndOfTurn() {
        Permanent falcon = addCreatureReady(player1, new LoyalGyrfalcon());

        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isTrue();
    }
}
