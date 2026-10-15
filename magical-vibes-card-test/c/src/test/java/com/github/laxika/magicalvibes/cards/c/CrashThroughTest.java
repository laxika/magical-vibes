package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrashThrough.class, FeralProwler.class})
class CrashThroughTest extends BaseCardTest {

    @Test
    @DisplayName("Crash Through grants trample to own creatures and draws a card")
    void grantsTrampleAndDraws() {
        Permanent own = addCreatureReady(player1, new FeralProwler());
        Permanent enemy = addCreatureReady(player2, new FeralProwler());
        harness.setHand(player1, List.of(new CrashThrough()));
        harness.addMana(player1, ManaColor.RED, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(own.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(enemy.hasKeyword(Keyword.TRAMPLE)).isFalse();
        // Started at handBefore, cast one card (-1), drew one (+1) => back to handBefore.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Crash Through trample wears off at end of turn")
    void trampleWearsOff() {
        Permanent own = addCreatureReady(player1, new FeralProwler());
        harness.setHand(player1, List.of(new CrashThrough()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(own.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(own.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Crash Through draws a card even without creatures")
    void drawsWithoutCreatures() {
        FeralProwler drawn = new FeralProwler();
        harness.setLibrary(player1, List.of(drawn, new FeralProwler()));
        harness.setHand(player1, List.of(new CrashThrough()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Crash Through does not grant trample to creatures entering later")
    void doesNotAffectLaterCreatures() {
        Permanent original = addCreatureReady(player1, new FeralProwler());
        harness.setHand(player1, List.of(new CrashThrough()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent later = harness.addToBattlefieldAndReturn(player1, new FeralProwler());

        assertThat(original.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(later.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
