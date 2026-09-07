package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NewGenerationsTechnique.class, Forest.class, GrizzlyBears.class, Plains.class})
class NewGenerationsTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for up to two basic lands and puts them onto the battlefield tapped")
    void searchesForUpToTwoBasicLands() {
        Card plains = new Plains();
        Card forest = new Forest();
        Card nonLand = new GrizzlyBears();
        setLibrary(plains, forest, nonLand);
        harness.setHand(player1, List.of(new NewGenerationsTechnique()));
        addManaForNormalCost();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(plains, forest);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonLand);
    }

    @Test
    @DisplayName("May find only one basic land")
    void searchesForOnlyOneAvailableBasicLand() {
        Card forest = new Forest();
        setLibrary(forest, new GrizzlyBears());
        harness.setHand(player1, List.of(new NewGenerationsTechnique()));
        addManaForNormalCost();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker")
    void sneakReturnsAnUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        setLibrary(new GrizzlyBears());
        harness.setHand(player1, List.of(new NewGenerationsTechnique()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    private void addManaForNormalCost() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
