package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.EnhancedSurveillance;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.OglorDevotedAssistant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Narcomoeba.class, Millstone.class, OglorDevotedAssistant.class, EnhancedSurveillance.class})
class NarcomoebaTest extends BaseCardTest {

    @Test
    @DisplayName("When milled, Narcomoeba may return itself from the graveyard to the battlefield")
    void mayReturnItselfFromGraveyardWhenMilled() {
        Card narcomoeba = setUpMillAndReturnCard();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(narcomoeba.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(narcomoeba.getId()));
    }

    @Test
    @DisplayName("Declining Narcomoeba's mill trigger leaves it in the graveyard")
    void decliningMillTriggerLeavesItInGraveyard() {
        Card narcomoeba = setUpMillAndReturnCard();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(narcomoeba.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(narcomoeba.getId()));
    }

    @Test
    @DisplayName("When put into its graveyard from its library without being milled, Narcomoeba may return itself")
    void mayReturnItselfWhenPutIntoGraveyardFromLibraryWithoutBeingMilled() {
        Card narcomoeba = new Narcomoeba();
        harness.addToBattlefield(player1, new OglorDevotedAssistant());
        harness.setLibrary(player1, List.of(narcomoeba, new Narcomoeba()));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(narcomoeba.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(narcomoeba.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(narcomoeba.getId()));
    }

    @Test
    @DisplayName("An opponent milling Narcomoeba gives its owner the choice and the creature")
    void opponentMillingReturnsToOwner() {
        Card narcomoeba = new Narcomoeba();
        harness.setLibrary(player1, List.of(narcomoeba));
        harness.addToBattlefield(player2, new Millstone());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, player1.getId());

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Narcomoeba");
        harness.assertNotOnBattlefield(player2, "Narcomoeba");
        harness.assertNotInGraveyard(player1, "Narcomoeba");
        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Two Narcomoebas milled together each offer an independent choice")
    void twoMilledCopiesOfferIndependentChoices() {
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new Narcomoeba(), new Narcomoeba()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player1.getId());

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Narcomoeba")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Narcomoeba")).hasSize(1);
    }

    @Test
    @DisplayName("Narcomoeba shuffled out of the graveyard cannot return from the library")
    void cannotReturnAfterLeavingGraveyard() {
        Card narcomoeba = setUpMillAndReturnCard();
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Narcomoeba");

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player1, "Narcomoeba");
        harness.assertNotInGraveyard(player1, "Narcomoeba");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(narcomoeba);
    }

    @Test
    @DisplayName("An old Narcomoeba trigger cannot return a copy that left and reentered the graveyard")
    void oldTriggerCannotReturnNewGraveyardObject() {
        Card narcomoeba = setUpMillAndReturnCard();
        harness.addToBattlefield(player1, new EnhancedSurveillance());
        harness.addToBattlefield(player1, new Millstone());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Narcomoeba");

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(narcomoeba);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInGraveyard(player1, "Narcomoeba");
        harness.assertNotOnBattlefield(player1, "Narcomoeba");
    }

    private Card setUpMillAndReturnCard() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        millstone.setSummoningSick(false);

        Card narcomoeba = new Narcomoeba();
        harness.setLibrary(player1, List.of(narcomoeba));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player1.getId());
        return narcomoeba;
    }
}
