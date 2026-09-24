package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonologueTax.class, GrizzlyBears.class, LlanowarElves.class})
class MonologueTaxTest extends BaseCardTest {

    @Test
    void createsATreasureWhenAnOpponentCastsTheirSecondSpell() {
        harness.addToBattlefield(player1, new MonologueTax());
        prepareOpponentTurn(List.of(new GrizzlyBears(), new LlanowarElves()));

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void doesNotTriggerForTheFirstOrThirdOpponentSpell() {
        harness.addToBattlefield(player1, new MonologueTax());
        prepareOpponentTurn(List.of(new LlanowarElves(), new GrizzlyBears(), new LlanowarElves()));

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void doesNotTriggerForTheControllerCastingTheirSecondSpell() {
        harness.addToBattlefield(player1, new MonologueTax());
        harness.setHand(player1, List.of(new LlanowarElves(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void prepareOpponentTurn(List<Card> hand) {
        harness.setHand(player2, hand);
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
