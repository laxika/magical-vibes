package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Ready // Willing is one card whose two halves (and their fusion) are the three modes of a single
 * modal instant, each paying its own total cost.
 */
class ReadyWillingTest extends BaseCardTest {

    private static final int READY = 0;
    private static final int WILLING = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Ready grants indestructible to your creatures and untaps them")
    void readyGrantsIndestructibleAndUntaps() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        mine.tap();
        theirs.tap();

        harness.setHand(player1, List.of(new ReadyWilling()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, READY, List.of());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mine, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(mine.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(theirs.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Willing grants deathtouch and lifelink to your creatures only")
    void willingGrantsDeathtouchAndLifelink() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ReadyWilling()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, WILLING, List.of());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mine, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Willing is castable off its own {1}{W}{B} cost")
    void willingIsPaidWithItsOwnCost() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ReadyWilling()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, WILLING, List.of());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mine, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Fuse resolves Ready then Willing")
    void fuseResolvesBothHalves() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        mine.tap();

        harness.setHand(player1, List.of(new ReadyWilling()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, FUSE, List.of());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mine, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.LIFELINK)).isTrue();
        assertThat(mine.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Fuse cannot be cast for only one half's mana")
    void fuseRequiresBothHalvesCost() {
        harness.setHand(player1, List.of(new ReadyWilling()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FUSE, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Keyword grants wear off at end of turn")
    void wearsOff() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ReadyWilling()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, FUSE, List.of());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mine, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.LIFELINK)).isFalse();
    }
}
