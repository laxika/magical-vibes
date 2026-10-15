package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PlayWithFire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathbonnetSprout.class, DeathbonnetHulk.class, DawnhartRejuvenator.class, PlayWithFire.class})
class DeathbonnetSproutTest extends BaseCardTest {

    @Test
    @DisplayName("Mills a card and transforms when the third creature card reaches the graveyard")
    void millsAndTransformsAtThreeCreatureCards() {
        Permanent sprout = addCreatureReady(player1, new DeathbonnetSprout());
        Card milledCreature = new DawnhartRejuvenator();
        harness.setGraveyard(player1, List.of(new DawnhartRejuvenator(), new DawnhartRejuvenator()));
        harness.setLibrary(player1, List.of(milledCreature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCreature);
        assertThat(sprout.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform when the milled card is not a creature")
    void doesNotTransformWithoutThreeCreatureCards() {
        Permanent sprout = addCreatureReady(player1, new DeathbonnetSprout());
        Card milledNoncreature = new PlayWithFire();
        harness.setGraveyard(player1, List.of(new DawnhartRejuvenator(), new DawnhartRejuvenator()));
        harness.setLibrary(player1, List.of(milledNoncreature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledNoncreature);
        assertThat(sprout.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("May exile a creature card from any graveyard and put a counter on itself")
    void exilesCreatureFromAnyGraveyardAndPutsCounterOnSource() {
        Permanent hulk = addCreatureReady(player1, new DeathbonnetHulk());
        Card opponentCreature = new DawnhartRejuvenator();
        harness.setGraveyard(player2, List.of(opponentCreature));

        chooseGraveyardCard(opponentCreature);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCreature);
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself when a noncreature card is exiled")
    void doesNotPutCounterForNoncreature() {
        Permanent hulk = addCreatureReady(player1, new DeathbonnetHulk());
        Card opponentNoncreature = new PlayWithFire();
        harness.setGraveyard(player2, List.of(opponentNoncreature));

        chooseGraveyardCard(opponentNoncreature);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentNoncreature);
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("May decline to exile a card")
    void mayDeclineExile() {
        Permanent hulk = addCreatureReady(player1, new DeathbonnetHulk());
        Card opponentCreature = new DawnhartRejuvenator();
        harness.setGraveyard(player2, List.of(opponentCreature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Transforms with an empty library when three creatures are already in its graveyard")
    void transformsEvenWhenThereIsNothingToMill() {
        Permanent sprout = addCreatureReady(player1, new DeathbonnetSprout());
        harness.setGraveyard(player1, List.of(
                new DawnhartRejuvenator(), new DawnhartRejuvenator(), new DawnhartRejuvenator()));
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(sprout.isTransformed()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Opponent's creature cards do not count toward transforming")
    void ignoresOpponentGraveyardForThreshold() {
        Permanent sprout = addCreatureReady(player1, new DeathbonnetSprout());
        Card milledCreature = new DawnhartRejuvenator();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(
                new DawnhartRejuvenator(), new DawnhartRejuvenator(), new DawnhartRejuvenator()));
        harness.setLibrary(player1, List.of(milledCreature));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledCreature);
        assertThat(sprout.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transformation does not trigger the Hulk until the next upkeep")
    void transformedHulkExilesOnNextUpkeepWithoutMilling() {
        Permanent sprout = addCreatureReady(player1, new DeathbonnetSprout());
        Card milledCreature = new DawnhartRejuvenator();
        Card libraryCard = new PlayWithFire();
        harness.setGraveyard(player1, List.of(new DawnhartRejuvenator(), new DawnhartRejuvenator()));
        harness.setLibrary(player1, List.of(milledCreature, libraryCard));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(sprout.isTransformed()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(sprout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        chooseGraveyardCard(milledCreature);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(milledCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(milledCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(sprout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sprout.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Hulk's upkeep resolves without a choice when both graveyards are empty")
    void hulkHandlesEmptyGraveyards() {
        Permanent hulk = addCreatureReady(player1, new DeathbonnetHulk());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Neither face triggers during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        Permanent sprout = addCreatureReady(player1, new DeathbonnetSprout());
        Permanent hulk = addCreatureReady(player1, new DeathbonnetHulk());
        Card libraryCard = new DawnhartRejuvenator();
        Card graveyardCard = new DawnhartRejuvenator();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player2, List.of(graveyardCard));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(sprout.isTransformed()).isFalse();
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
    @Test
    @DisplayName("Transforms after milling a noncreature when the threshold was already met")
    void transformsWithThreeCreaturesAlreadyInGraveyard() {
        Permanent sprout = addCreatureReady(player1, new DeathbonnetSprout());
        Card milledNoncreature = new PlayWithFire();
        harness.setGraveyard(player1, List.of(
                new DawnhartRejuvenator(), new DawnhartRejuvenator(), new DawnhartRejuvenator()));
        harness.setLibrary(player1, List.of(milledNoncreature));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledNoncreature).hasSize(4);
        assertThat(sprout.isTransformed()).isTrue();
    }
    private void chooseGraveyardCard(Card card) {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        resolveAllTriggers();
    }
}
