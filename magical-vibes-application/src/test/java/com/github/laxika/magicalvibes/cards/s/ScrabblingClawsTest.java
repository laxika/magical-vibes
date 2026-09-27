package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HematiteGolem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrabblingClaws.class, HematiteGolem.class})
class ScrabblingClawsTest extends BaseCardTest {

    @Test
    void tapsToMakeTargetPlayerExileCardFromTheirGraveyard() {
        harness.addToBattlefield(player1, new ScrabblingClaws());
        Card card = new HematiteGolem();
        harness.setGraveyard(player2, List.of(card));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Hematite Golem");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(card);
    }

    @Test
    void sacrificesItselfExilesTargetCardAndDraws() {
        Card graveyardCard = new HematiteGolem();
        Card libraryCard = new HematiteGolem();
        harness.addToBattlefield(player1, new ScrabblingClaws());
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(libraryCard);
        harness.assertNotOnBattlefield(player1, "Scrabbling Claws");
    }

    @Test
    void cannotActivateSacrificeAbilityWithoutGraveyardTarget() {
        harness.addToBattlefield(player1, new ScrabblingClaws());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappingAbilityCanTargetPlayerWithEmptyGraveyard() {
        Permanent claws = harness.addToBattlefieldAndReturn(player1, new ScrabblingClaws());

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        assertThat(claws.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void tappingAbilityLetsTargetPlayerChooseWhichGraveyardCardToExile() {
        Card firstCard = new HematiteGolem();
        Card chosenCard = new HematiteGolem();
        harness.addToBattlefield(player1, new ScrabblingClaws());
        harness.setGraveyard(player2, List.of(firstCard, chosenCard));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleGraveyardCardChosen(player2,
                gd.playerGraveyards.get(player2.getId()).indexOf(chosenCard));

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(firstCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosenCard);
    }

    @Test
    void sacrificeAbilityPaysItsManaCost() {
        Card graveyardCard = new HematiteGolem();
        harness.addToBattlefield(player1, new ScrabblingClaws());
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(graveyardCard.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    void sacrificeAbilityCanExileFromItsControllersGraveyard() {
        Card graveyardCard = new HematiteGolem();
        harness.addToBattlefield(player1, new ScrabblingClaws());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(graveyardCard);
    }

    @Test
    void sacrificeAbilityDoesNotDrawWhenItsTargetLeavesTheGraveyardBeforeResolution() {
        Card graveyardCard = new HematiteGolem();
        Card libraryCard = new HematiteGolem();
        harness.addToBattlefield(player1, new ScrabblingClaws());
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(graveyardCard.getId()));
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(graveyardCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Scrabbling Claws");
    }
}
