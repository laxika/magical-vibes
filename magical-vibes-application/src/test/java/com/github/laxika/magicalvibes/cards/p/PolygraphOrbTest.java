package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PolygraphOrb.class, Forest.class, GrizzlyBears.class, LeoninScimitar.class})
class PolygraphOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by putting two of the top four cards into hand and the rest into the graveyard")
    void entersAndSelectsTwoCards() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        Card fourth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new PolygraphOrb()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Collect evidence 3 pays the activation cost and makes an optionless opponent lose 3 life")
    void collectEvidenceAndLoseLife() {
        Card evidenceOne = new GrizzlyBears();
        Card evidenceTwo = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(evidenceOne, evidenceTwo));
        harness.addToBattlefield(player1, new PolygraphOrb());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(evidenceOne.getId(), evidenceTwo.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(evidenceOne, evidenceTwo);
    }

    @Test
    @DisplayName("The activated ability allows an opponent to sacrifice a creature, not a noncreature permanent")
    void opponentSacrificesCreatureInsteadOfLosingLife() {
        Card evidenceOne = new GrizzlyBears();
        Card evidenceTwo = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(evidenceOne, evidenceTwo));
        harness.addToBattlefield(player1, new PolygraphOrb());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new LeoninScimitar());
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player2, creature);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(evidenceOne.getId(), evidenceTwo.getId()));
        harness.passBothPriorities();

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty library does not prevent the entrance ability's life loss")
    void emptyLibraryStillLosesLife() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new PolygraphOrb()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("With only one library card, put it into hand and still lose 2 life")
    void shortLibraryPutsAvailableCardIntoHand() {
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new PolygraphOrb()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Opponent may discard a card instead of losing life")
    void opponentDiscardsInsteadOfLosingLife() {
        Card evidence = new PolygraphOrb();
        Card discarded = new Forest();
        harness.setGraveyard(player1, List.of(evidence));
        harness.addToBattlefield(player1, new PolygraphOrb());
        harness.setHand(player2, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(evidence.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(evidence);
        harness.passBothPriorities();
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent may choose life loss even when they can discard or sacrifice")
    void opponentMayDeclineBothAlternatives() {
        Card evidence = new PolygraphOrb();
        Card retained = new Forest();
        harness.setGraveyard(player1, List.of(evidence));
        harness.addToBattlefield(player1, new PolygraphOrb());
        harness.setHand(player2, List.of(retained));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(evidence.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Lose 3 life");

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An evidence selection below mana value 3 is rejected without exiling cards")
    void insufficientEvidenceSelectionIsRejected() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addToBattlefield(player1, new PolygraphOrb());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
    }
}
