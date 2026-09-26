package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DeterminedIteration.class)
class DeterminedIterationTest extends BaseCardTest {

    @Test
    @DisplayName("Populates at the beginning of combat and gives the new token haste")
    void populatesWithHasteAtBeginningOfCombat() {
        addDeterminedIteration(player1);
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier Token")).hasSize(2);
        assertThat(findPermanents(player1, "Soldier Token")).filteredOn(
                token -> gqs.hasKeyword(gd, token, Keyword.HASTE)).hasSize(1);
    }

    @Test
    @DisplayName("Sacrifices the populated token at the beginning of the next end step")
    void sacrificesPopulatedTokenAtNextEndStep() {
        addDeterminedIteration(player1);
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Soldier Token")).hasSize(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier Token")).hasSize(1);
    }

    @Test
    @DisplayName("Does nothing without a creature token")
    void doesNothingWithoutCreatureToken() {
        addDeterminedIteration(player1);

        advanceToBeginningOfCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        addDeterminedIteration(player1);
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));

        advanceToBeginningOfCombat(player2);

        assertThat(findPermanents(player1, "Soldier Token")).hasSize(1);
    }

    private Permanent addDeterminedIteration(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DeterminedIteration());
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private static Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
