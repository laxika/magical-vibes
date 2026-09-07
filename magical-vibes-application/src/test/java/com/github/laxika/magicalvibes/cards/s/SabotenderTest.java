package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sabotender.class, Forest.class, GrizzlyBears.class})
class SabotenderTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each opponent when a land you control enters")
    void ownLandDealsDamageToOpponent() {
        harness.addToBattlefield(player1, new Sabotender());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's land or a nonland permanent")
    void ignoresOpponentsLandsAndNonlands() {
        harness.addToBattlefield(player1, new Sabotender());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        assertThat(gd.stack).isEmpty();

        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
