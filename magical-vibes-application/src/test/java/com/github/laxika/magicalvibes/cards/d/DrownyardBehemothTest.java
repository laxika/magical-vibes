package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrownyardBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Emerge sacrifices a creature and reduces the generic cost by its mana value")
    void emergeSacrificesAndReducesCost() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new DrownyardBehemoth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drownyard Behemoth");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Hexproof lasts only while Drownyard Behemoth entered this turn")
    void hexproofExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new DrownyardBehemoth()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent behemoth = findPermanent(player1, "Drownyard Behemoth");
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HEXPROOF)).isFalse();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCard(gd, player2, 0, 0, behemoth.getId(), null);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent cannot target Drownyard Behemoth while it has hexproof")
    void opponentCannotTargetWhileItHasHexproof() {
        harness.setHand(player1, List.of(new DrownyardBehemoth()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent behemoth = findPermanent(player1, "Drownyard Behemoth");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, behemoth.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }
}
