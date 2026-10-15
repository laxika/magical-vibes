package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DrownyardExplorers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FuneralCharm;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnshakableTail.class, DrownyardExplorers.class, GrizzlyBears.class,
        Millstone.class, FuneralCharm.class})
class UnshakableTailTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and surveils one")
    void entersAndSurveils() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.enterBattlefieldAndReturn(player1, new UnshakableTail());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Surveils one at the beginning of its controller's upkeep")
    void surveilsAtUpkeep() {
        harness.enterBattlefieldAndReturn(player1, new UnshakableTail());
        resolveInitialSurveil();

        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Investigates once when one or more of its controller's creature cards are milled")
    void investigatesOncePerMillEventWithCreatureCards() {
        harness.enterBattlefieldAndReturn(player1, new UnshakableTail());
        resolveInitialSurveil();
        harness.enterBattlefieldAndReturn(player1, new Millstone());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllTriggers();

        assertThat(countClues()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Clue and two mana return it from the graveyard to its owner's hand")
    void returnsFromGraveyardBySacrificingClue() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new DrownyardExplorers());
        resolveAllTriggers();

        UnshakableTail tail = new UnshakableTail();
        harness.setGraveyard(player1, List.of(tail));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Unshakable Tail");
        harness.assertNotInGraveyard(player1, "Unshakable Tail");
        assertThat(countClues()).isZero();
    }

    @Test
    @DisplayName("Surveilling a creature into the graveyard investigates")
    void investigatesFromItsOwnSurveil() {
        Card creature = new UnshakableTail();
        harness.setLibrary(player1, List.of(creature));
        harness.enterBattlefieldAndReturn(player1, new UnshakableTail());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(countClues()).isEqualTo(1);
    }

    @Test
    @DisplayName("Keeping the surveilled creature on top does not investigate")
    void keepingCreatureOnTopDoesNotInvestigate() {
        Card creature = new UnshakableTail();
        harness.setLibrary(player1, List.of(creature));
        harness.enterBattlefieldAndReturn(player1, new UnshakableTail());
        resolveInitialSurveil();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(countClues()).isZero();
    }

    @Test
    @DisplayName("Putting a noncreature into the graveyard with surveil does not investigate")
    void surveillingNoncreatureDoesNotInvestigate() {
        Card artifact = new Millstone();
        harness.setLibrary(player1, List.of(artifact));
        harness.enterBattlefieldAndReturn(player1, new UnshakableTail());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact);
        assertThat(countClues()).isZero();
    }

    @Test
    @DisplayName("An opponent milling creatures does not investigate")
    void opponentsCreatureMillDoesNotInvestigate() {
        harness.enterBattlefieldAndReturn(player1, new UnshakableTail());
        resolveInitialSurveil();
        harness.enterBattlefieldAndReturn(player1, new Millstone());
        Card creature = new UnshakableTail();
        harness.setLibrary(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature);
        assertThat(countClues()).isZero();
    }

    @Test
    @DisplayName("Does not surveil on the opponent's upkeep")
    void doesNotSurveilOnOpponentsUpkeep() {
        harness.enterBattlefieldAndReturn(player1, new UnshakableTail());
        resolveInitialSurveil();
        Card creature = new UnshakableTail();
        harness.setLibrary(player1, List.of(creature));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(countClues()).isZero();
    }

    @Test
    @DisplayName("A non-Clue artifact cannot pay the graveyard ability's sacrifice cost")
    void cannotSacrificeAnotherArtifactInsteadOfClue() {
        harness.enterBattlefieldAndReturn(player1, new Millstone());
        harness.setGraveyard(player1, List.of(new UnshakableTail()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Unshakable Tail");
        harness.assertNotInHand(player1, "Unshakable Tail");
        harness.assertOnBattlefield(player1, "Millstone");
    }

    @Test
    @CardUsed({UnshakableTail.class, DrownyardExplorers.class, FuneralCharm.class})
    @DisplayName("An older return ability cannot return the card after it leaves and reenters the graveyard")
    void olderAbilityCannotReturnNewGraveyardObject() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new DrownyardExplorers());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new DrownyardExplorers());
        resolveAllTriggers();
        UnshakableTail tail = new UnshakableTail();
        harness.setGraveyard(player1, List.of(tail));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Clue").getId());
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Unshakable Tail");
        harness.assertNotInGraveyard(player1, "Unshakable Tail");

        harness.setHand(player2, List.of(new FuneralCharm()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, 0, player1.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null) {
            harness.handleCardChosen(player1, 0);
        }
        harness.assertInGraveyard(player1, "Unshakable Tail");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Unshakable Tail");
        harness.assertNotInHand(player1, "Unshakable Tail");
        assertThat(countClues()).isZero();
    }

    private long countClues() {
        return countPermanents(player1, "Clue");
    }

    private void resolveInitialSurveil() {
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }
}
