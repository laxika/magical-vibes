package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LindenTheSteadfastQueen.class)
class LindenTheSteadfastQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life for each white creature that attacks")
    void gainsLifeForEachWhiteCreatureThatAttacks() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new LindenTheSteadfastQueen());
        addCreatureReady(player1, creature(CardColor.WHITE, 2));
        addCreatureReady(player1, creature(CardColor.WHITE, 2));

        declareAttackers(List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack).allSatisfy(entry ->
                assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Does not trigger for a nonwhite creature that attacks")
    void doesNotTriggerForNonwhiteCreature() {
        addCreatureReady(player1, new LindenTheSteadfastQueen());
        addCreatureReady(player1, creature(CardColor.GREEN, 2));

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's white creature")
    void doesNotTriggerForOpponentsWhiteCreature() {
        addCreatureReady(player1, new LindenTheSteadfastQueen());
        addCreatureReady(player2, creature(CardColor.WHITE, 2));

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    private Card creature(CardColor color, int power) {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setColors(List.of(color));
        creature.setPower(power);
        creature.setToughness(power);
        return creature;
    }
}
