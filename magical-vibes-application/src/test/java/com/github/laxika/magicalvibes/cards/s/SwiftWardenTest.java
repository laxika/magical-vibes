package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
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

class SwiftWardenTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives a Merfolk you control hexproof until end of turn")
    void etbGrantsHexproofToTargetMerfolk() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        harness.setHand(player1, List.of(new SwiftWarden()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        gs.playCard(gd, player1, 0, 0, merfolk.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(merfolk.getGrantedKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Granted hexproof wears off at end of turn")
    void grantedHexproofWearsOffAtEndOfTurn() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        harness.setHand(player1, List.of(new SwiftWarden()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        gs.playCard(gd, player1, 0, 0, merfolk.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(merfolk.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Cannot target an opponent's Merfolk")
    void cannotTargetOpponentMerfolk() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new SwiftWarden()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, merfolk.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Merfolk you control");
    }

    @Test
    @DisplayName("Can cast during the opponent's turn because it has flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new SwiftWarden()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        gs.passPriority(gd, player2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
