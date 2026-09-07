package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({CemeteryProtector.class, Forest.class, GrizzlyBears.class})
class CemeteryProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Its ETB exiles a chosen card from any graveyard and remembers it")
    void exilesAndImprintsChosenCard() {
        Card card = new GrizzlyBears();
        Permanent protector = enterProtectorWith(card);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
        assertThat(gd.getImprintedCard(protector.getCard())).isSameAs(card);
    }

    @Test
    @DisplayName("Casting a spell that shares a card type creates a Human token")
    void matchingSpellCreatesHuman() {
        enterProtectorWith(new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
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
        enterProtectorWith(new GrizzlyBears());
        playLand(new Forest());

        assertThat(humanTokens(player1)).isEmpty();
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
