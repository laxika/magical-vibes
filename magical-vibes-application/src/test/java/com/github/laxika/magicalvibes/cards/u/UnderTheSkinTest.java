package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CautiousSurvivor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SayItsName;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderTheSkin.class, CautiousSurvivor.class, Forest.class, SayItsName.class})
class UnderTheSkinTest extends BaseCardTest {

    @Test
    void manifestsDreadThenMayReturnPermanentFromGraveyard() {
        Card manifestedCard = new CautiousSurvivor();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new UnderTheSkin()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void mayReturnOnlyPermanentCards() {
        Card manifestedCard = new CautiousSurvivor();
        Card graveyardCard = new Forest();
        Card nonPermanentCard = new SayItsName();
        harness.setHand(player1, List.of(new UnderTheSkin()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.setGraveyard(player1, List.of(nonPermanentCard));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).validIndices())
                .containsExactly(1);
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Say Its Name");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void mayReturnCanBeDeclined() {
        Card manifestedCard = new CautiousSurvivor();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new UnderTheSkin()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    void returnsAnExistingPermanentWhenLibraryIsEmpty() {
        Card graveyardCard = new CautiousSurvivor();
        harness.setHand(player1, List.of(new UnderTheSkin()));
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Cautious Survivor");
        harness.assertNotInGraveyard(player1, "Cautious Survivor");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void manifestsTheOnlyLibraryCardAndStillReturnsAnExistingPermanent() {
        Card manifestedCard = new SayItsName();
        harness.setHand(player1, List.of(new UnderTheSkin()));
        harness.setLibrary(player1, List.of(manifestedCard));
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Say Its Name");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canReturnAPermanentThatWasAlreadyInTheGraveyard() {
        Card manifestedCard = new CautiousSurvivor();
        Card milledCard = new Forest();
        harness.setHand(player1, List.of(new UnderTheSkin()));
        harness.setLibrary(player1, List.of(manifestedCard, milledCard));
        harness.setGraveyard(player1, List.of(new CautiousSurvivor()));
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Cautious Survivor");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    void completesWithoutReturningAnythingWhenNoPermanentIsAvailable() {
        Card manifestedCard = new CautiousSurvivor();
        Card milledCard = new SayItsName();
        harness.setHand(player1, List.of(new UnderTheSkin()));
        harness.setLibrary(player1, List.of(manifestedCard, milledCard));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInGraveyard(player1, "Say Its Name");
        harness.assertInGraveyard(player1, "Under the Skin");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
