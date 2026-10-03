package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({CemeteryProtector.class, Forest.class, DawnhartDisciple.class, Panharmonicon.class})
class CemeteryProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Its ETB exiles a chosen card from any graveyard and remembers it")
    void exilesAndImprintsChosenCard() {
        Card card = new DawnhartDisciple();
        Permanent protector = enterProtectorWith(card);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
        assertThat(gd.getImprintedCard(protector.getCard())).isSameAs(card);
    }

    @Test
    void canExileFromItsControllersGraveyard() {
        Card card = new Forest();
        harness.setGraveyard(player1, List.of(card));
        harness.enterBattlefieldAndReturn(player1, new CemeteryProtector());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        playLand(new Forest());
        assertThat(humanTokens(player1)).hasSize(1);
    }

    @Test
    void canBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new CemeteryProtector(), "{2}{W}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cemetery Protector");
    }

    @Test
    @DisplayName("Casting a spell that shares a card type creates a Human token")
    void matchingSpellCreatesHuman() {
        enterProtectorWith(new DawnhartDisciple());
        harness.castFromHand(player1, new DawnhartDisciple(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(humanTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Playing a land that shares a card type creates a Human token")
    void matchingLandCreatesHuman() {
        enterProtectorWith(new Forest());
        playLand(new Forest());

        assertThat(humanTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("A card with no shared card type does not create a token")
    void nonmatchingLandDoesNotCreateHuman() {
        enterProtectorWith(new DawnhartDisciple());
        playLand(new Forest());

        assertThat(humanTokens(player1)).isEmpty();
    }

    @Test
    void nonmatchingSpellDoesNotCreateHuman() {
        enterProtectorWith(new Forest());
        harness.castFromHand(player1, new DawnhartDisciple(), "{1}{G}");
        resolveAllTriggers();

        assertThat(humanTokens(player1)).isEmpty();
    }

    @Test
    void emptyGraveyardsLeaveNothingToMatch() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new CemeteryProtector());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        playLand(new Forest());
        harness.castFromHand(player1, new DawnhartDisciple(), "{1}{G}");
        resolveAllTriggers();

        assertThat(humanTokens(player1)).isEmpty();
    }

    @Test
    void matchingOpponentSpellDoesNotCreateHuman() {
        enterProtectorWith(new DawnhartDisciple());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new DawnhartDisciple(), "{1}{G}");
        resolveAllTriggers();

        assertThat(humanTokens(player1)).isEmpty();
        assertThat(humanTokens(player2)).isEmpty();
    }

    @Test
    void matchingOpponentLandDoesNotCreateHuman() {
        enterProtectorWith(new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(humanTokens(player1)).isEmpty();
        assertThat(humanTokens(player2)).isEmpty();
    }

    @Test
    void matchingSpellTriggerResolvesAfterProtectorLeaves() {
        Permanent protector = enterProtectorWith(new DawnhartDisciple());
        harness.castFromHand(player1, new DawnhartDisciple(), "{1}{G}");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, protector));
        harness.passBothPriorities();

        assertThat(humanTokens(player1)).hasSize(1);
        assertThat(findPermanents(player1, "Dawnhart Disciple")).isEmpty();
    }

    @Test
    @CardUsed({Panharmonicon.class})
    void doubledEntryRemembersBothExiledCardTypes() {
        harness.addToBattlefield(player1, new Panharmonicon());
        Card land = new Forest();
        Card creature = new DawnhartDisciple();
        harness.setGraveyard(player2, List.of(land, creature));
        harness.enterBattlefieldAndReturn(player1, new CemeteryProtector());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(land, creature);
        playLand(new Forest());

        assertThat(humanTokens(player1)).hasSize(1);
    }

    private Permanent enterProtectorWith(Card card) {
        harness.setGraveyard(player2, List.of(card));
        Permanent protector = harness.enterBattlefieldAndReturn(player1, new CemeteryProtector());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(card.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        return protector;
    }

    private void playLand(Card land) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(land));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
    }

    private List<Permanent> humanTokens(Player player) {
        return findPermanents(player, "Human").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
