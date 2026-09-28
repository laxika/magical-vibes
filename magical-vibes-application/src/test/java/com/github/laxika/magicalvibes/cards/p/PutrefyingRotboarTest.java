package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PutrefyingRotboar.class, Forest.class, GrizzlyBears.class})
class PutrefyingRotboarTest extends BaseCardTest {

    @Test
    void boarAttackMakesNonlandHandCardCostOneLifeToCast() {
        addCreatureReady(player1, new PutrefyingRotboar());
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void nonBoarAttackDoesNotMarkDefendingHand() {
        addCreatureReady(player1, new PutrefyingRotboar());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
