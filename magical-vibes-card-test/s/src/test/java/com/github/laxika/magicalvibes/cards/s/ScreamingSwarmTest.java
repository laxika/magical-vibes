package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreamingSwarm.class, SnarlingWolf.class, Abrade.class})
class ScreamingSwarmTest extends BaseCardTest {

    @Test
    void millsOneCardPerAttackingCreature() {
        addCreatureReady(player1, new ScreamingSwarm());
        addCreatureReady(player1, new SnarlingWolf());
        addCreatureReady(player1, new SnarlingWolf());
        harness.setLibrary(player2, libraryWithCards(10));

        declareAttackers(player1, List.of(1, 2));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(8);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void canTargetYourself() {
        addCreatureReady(player1, new ScreamingSwarm());
        addCreatureReady(player1, new SnarlingWolf());
        harness.setLibrary(player1, libraryWithCards(5));

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void graveyardAbilityPutsThisCardSecondFromTop() {
        ScreamingSwarm swarm = new ScreamingSwarm();
        harness.setGraveyard(player1, List.of(swarm));
        harness.setLibrary(player1, libraryWithCards(3));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(4);
        assertThat(library.get(1)).isSameAs(swarm);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(swarm);
    }

    @Test
    void swarmCountsItselfWhenAttackingAlone() {
        addCreatureReady(player1, new ScreamingSwarm());
        harness.setLibrary(player2, libraryWithCards(5));

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void attackerCountIsPreservedWhenAnAttackerDiesBeforeResolution() {
        addCreatureReady(player1, new ScreamingSwarm());
        Permanent wolf = addCreatureReady(player1, new SnarlingWolf());
        harness.setLibrary(player2, libraryWithCards(5));
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castModalInstant(player2, 0, 0, List.of(wolf.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wolf);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof SnarlingWolf).hasSize(2);
    }

    @Test
    void opposingAttackDoesNotTriggerMill() {
        addCreatureReady(player1, new ScreamingSwarm());
        addCreatureReady(player2, new SnarlingWolf());
        harness.setLibrary(player1, libraryWithCards(5));
        harness.setLibrary(player2, libraryWithCards(5));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void millsOnlyAvailableCardsWhenLibraryIsShorterThanAttackerCount() {
        addCreatureReady(player1, new ScreamingSwarm());
        addCreatureReady(player1, new SnarlingWolf());
        addCreatureReady(player1, new SnarlingWolf());
        harness.setLibrary(player2, libraryWithCards(1));

        declareAttackers(player1, List.of(0, 1, 2));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void graveyardAbilityPutsCardOnTopOfEmptyLibrary() {
        ScreamingSwarm swarm = new ScreamingSwarm();
        harness.setGraveyard(player1, List.of(swarm));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(swarm);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void graveyardAbilityMovesOnlyTheActivatedCopy() {
        ScreamingSwarm first = new ScreamingSwarm();
        ScreamingSwarm second = new ScreamingSwarm();
        harness.setGraveyard(player1, List.of(first, second));
        List<Card> originalLibrary = libraryWithCards(1);
        harness.setLibrary(player1, originalLibrary);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(originalLibrary.getFirst(), second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    void multipleActivationsCannotMoveTheSameCardTwice() {
        ScreamingSwarm swarm = new ScreamingSwarm();
        harness.setGraveyard(player1, List.of(swarm));
        List<Card> originalLibrary = libraryWithCards(3);
        harness.setLibrary(player1, originalLibrary);
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(originalLibrary.get(0), swarm, originalLibrary.get(1), originalLibrary.get(2));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private List<Card> libraryWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new SnarlingWolf())
                .toList();
    }
}
