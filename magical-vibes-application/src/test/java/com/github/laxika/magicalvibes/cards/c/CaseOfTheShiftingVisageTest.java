package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaseOfTheShiftingVisage.class, GrizzlyBears.class})
class CaseOfTheShiftingVisageTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 at the beginning of your upkeep")
    void surveilsAtUpkeep() {
        harness.addToBattlefield(player1, new CaseOfTheShiftingVisage());
        var topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Solves at the beginning of the end step with fifteen graveyard cards")
    void solvesWithFifteenCardsInGraveyard() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(
                player1, new CaseOfTheShiftingVisage());
        setGraveyardSize(15);

        resolveEndStepTrigger();

        assertThat(casePermanent.isSolved()).isTrue();
    }

    @Test
    @DisplayName("Does not solve with fewer than fifteen graveyard cards")
    void doesNotSolveWithFewerThanFifteenCards() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(
                player1, new CaseOfTheShiftingVisage());
        setGraveyardSize(14);

        resolveEndStepTrigger();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    @DisplayName("Copies a nonlegendary creature spell as a token after solving")
    void copiesNonlegendaryCreatureSpellAsToken() {
        solveCase();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .toList();
        assertThat(bears).hasSize(2);
        assertThat(bears).anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(bears).anyMatch(permanent -> !permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Does not copy a legendary creature spell")
    void doesNotCopyLegendaryCreatureSpell() {
        solveCase();
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        harness.setHand(player1, List.of(legendaryBears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void solveCase() {
        harness.addToBattlefield(player1, new CaseOfTheShiftingVisage());
        setGraveyardSize(15);
        resolveEndStepTrigger();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void setGraveyardSize(int size) {
        harness.setGraveyard(player1, IntStream.range(0, size)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList());
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
