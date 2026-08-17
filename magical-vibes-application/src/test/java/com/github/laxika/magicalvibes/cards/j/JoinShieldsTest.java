package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JoinShieldsTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and protects your creatures only")
    void untapsAndProtectsOwnCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        mine.tap();
        theirs.tap();

        cast(player1);

        assertThat(mine.isTapped()).isFalse();
        assertThat(theirs.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent cannot target a creature with granted hexproof")
    void opponentCannotTargetProtectedCreature() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, mine.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Hexproof and indestructible wear off at end of turn")
    void protectionsWearOffAtEndOfTurn() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(player1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mine, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void cast(Player player) {
        harness.setHand(player, List.of(new JoinShields()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castInstant(player, 0);
        harness.passBothPriorities();
    }
}
