package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VinesoulSpider.class, Forest.class, GrizzlyBears.class})
class VinesoulSpiderTest extends BaseCardTest {

    @Test
    void putsALandFromTheLibraryIntoTheGraveyardAtEndStep() {
        harness.addToBattlefield(player1, new VinesoulSpider());
        Card forest = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, forest));

        advanceToEndStep(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void doesNothingWhenTheLibraryHasNoLand() {
        harness.addToBattlefield(player1, new VinesoulSpider());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        advanceToEndStep(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void doesNotTriggerDuringTheOpponentsEndStep() {
        harness.addToBattlefield(player1, new VinesoulSpider());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        advanceToEndStep(player2);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void usesItsControllersLibraryWhenControlledByTheSecondPlayer() {
        harness.addToBattlefield(player2, new VinesoulSpider());
        Card ownForest = new Forest();
        Card opponentsForest = new Forest();
        harness.setLibrary(player2, List.of(ownForest));
        harness.setLibrary(player1, List.of(opponentsForest));

        advanceToEndStep(player2);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(ownForest);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentsForest);
    }

    @Test
    void movesExactlyOneLandAndPreservesTheOrderOfTheRemainingCards() {
        harness.addToBattlefield(player1, new VinesoulSpider());
        Card firstCreature = new GrizzlyBears();
        Card firstLand = new Forest();
        Card secondCreature = new GrizzlyBears();
        Card secondLand = new Forest();
        Card thirdCreature = new GrizzlyBears();
        List<Card> library = List.of(firstCreature, firstLand, secondCreature, secondLand, thirdCreature);
        harness.setLibrary(player1, library);

        advanceToEndStep(player1);

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).hasSize(1);
        assertThat(graveyard.getFirst()).isIn(firstLand, secondLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(
                library.stream().filter(card -> card != graveyard.getFirst()).toList());
    }

    @Test
    void doesNothingWhenTheLibraryIsEmpty() {
        harness.addToBattlefield(player1, new VinesoulSpider());
        harness.setLibrary(player1, List.of());

        advanceToEndStep(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void selectsTheLandFromTheLibraryAtResolutionRatherThanAtTriggerTime() {
        harness.addToBattlefield(player1, new VinesoulSpider());
        Card originalForest = new Forest();
        Card replacementForest = new Forest();
        harness.setLibrary(player1, List.of(originalForest));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.setLibrary(player1, List.of(replacementForest));
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(replacementForest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void resolvesAfterTheSpiderLeavesTheBattlefield() {
        harness.addToBattlefield(player1, new VinesoulSpider());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void eachSpiderCreatesAnIndependentTrigger() {
        harness.addToBattlefield(player1, new VinesoulSpider());
        harness.addToBattlefield(player1, new VinesoulSpider());
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passBothPriorities();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
            harness.passBothPriorities();
        });

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(firstLand, secondLand);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            if (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            }
        });
    }
}
