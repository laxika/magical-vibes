package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvertTheSkiesTest extends BaseCardTest {

    /**
     * Player1 casts Invert the Skies on their own turn paying the given mana. {3}{G/U}, so
     * feed only green to spend {G}, only blue to spend {U}, or a mix to spend both.
     */
    private void castInvertTheSkies(int green, int blue) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new InvertTheSkies()));
        if (green > 0) harness.addMana(player1, ManaColor.GREEN, green);
        if (blue > 0) harness.addMana(player1, ManaColor.BLUE, blue);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent ownBears() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
    }

    private Permanent opponentHawk() {
        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Suntail Hawk"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("{G} spent: opponents' creatures lose flying, your creatures don't gain it")
    void greenSpentStripsOpponentFlying() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        castInvertTheSkies(4, 0); // all green → only {G} spent

        assertThat(gqs.hasKeyword(gd, opponentHawk(), Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownBears(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("{U} spent: your creatures gain flying, opponents' keep theirs")
    void blueSpentGrantsOwnFlying() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        castInvertTheSkies(0, 4); // all blue → only {U} spent

        assertThat(gqs.hasKeyword(gd, ownBears(), Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentHawk(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("{G} and {U} both spent: opponents lose flying and your creatures gain it")
    void bothColorsDoBoth() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        castInvertTheSkies(2, 2); // green and blue both spent

        assertThat(gqs.hasKeyword(gd, opponentHawk(), Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownBears(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        castInvertTheSkies(2, 2);
        assertThat(gqs.hasKeyword(gd, opponentHawk(), Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownBears(), Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentHawk(), Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBears(), Keyword.FLYING)).isFalse();
    }
}
