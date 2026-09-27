package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DeterminedIteration.class)
class DeterminedIterationTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, populate creates a hasty copy")
    void populatesWithHaste() {
        harness.addToBattlefield(player1, new DeterminedIteration());
        harness.addToBattlefield(player1, soldierToken());

        advanceToBeginningOfCombat(player1);
        resolveAllTriggers();

        List<Permanent> soldiers = soldierTokens(player1);
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).filteredOn(permanent -> gqs.hasKeyword(gd, permanent, Keyword.HASTE))
                .hasSize(1);
    }

    @Test
    @DisplayName("The populated copy is sacrificed at the next end step")
    void populatedCopyIsSacrificedAtNextEndStep() {
        harness.addToBattlefield(player1, new DeterminedIteration());
        harness.addToBattlefield(player1, soldierToken());

        advanceToBeginningOfCombat(player1);
        resolveAllTriggers();
        assertThat(soldierTokens(player1)).hasSize(2);

        advanceToEndStep(player1);

        assertThat(soldierTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("The controller chooses which creature token to populate")
    void choosesCreatureTokenToCopy() {
        harness.addToBattlefield(player1, new DeterminedIteration());
        harness.addToBattlefield(player1, soldierToken());
        harness.addToBattlefield(player1, anotherToken());

        advanceToBeginningOfCombat(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent soldier = soldierTokens(player1).getFirst();
        harness.handlePermanentChosen(player1, soldier.getId());

        assertThat(soldierTokens(player1)).hasSize(2);
        assertThat(anotherTokens(player1)).hasSize(1);
        assertThat(soldierTokens(player1)).filteredOn(permanent -> gqs.hasKeyword(gd, permanent, Keyword.HASTE))
                .hasSize(1);
    }

    private List<Permanent> soldierTokens(Player player) {
        return findPermanents(player, "Soldier Token").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private List<Permanent> anotherTokens(Player player) {
        return findPermanents(player, "Another Token").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        gd.interaction.clearAwaitingInput();
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    private static Card soldierToken() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }

    private static Card anotherToken() {
        Card card = new Card();
        card.setName("Another Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(2);
        card.setToughness(2);
        card.setToken(true);
        return card;
    }
}
