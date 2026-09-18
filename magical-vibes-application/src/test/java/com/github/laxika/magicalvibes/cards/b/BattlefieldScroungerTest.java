package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlefieldScrounger.class, MentalNote.class})
class BattlefieldScroungerTest extends BaseCardTest {

    @Test
    void putsThreeGraveyardCardsOnLibraryBottomAndBoostsOncePerTurn() {
        Permanent scrounger = addCreatureReady(player1, new BattlefieldScrounger());
        List<Card> graveyard = List.of(
                new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote());
        List<Card> selected = List.copyOf(graveyard.subList(0, 3));
        List<Card> remaining = List.copyOf(graveyard.subList(3, 10));
        harness.setGraveyard(player1, graveyard);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, selected.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scrounger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, scrounger)).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(selected);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatesWithExactlySevenCardsInGraveyard() {
        Permanent scrounger = addCreatureReady(player1, new BattlefieldScrounger());
        List<Card> graveyard = List.of(
                new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote());
        Card libraryCard = new MentalNote();
        List<Card> selected = List.of(graveyard.get(6), graveyard.get(0), graveyard.get(3));
        harness.setGraveyard(player1, graveyard);
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, selected.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scrounger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, scrounger)).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(graveyard.get(1), graveyard.get(2), graveyard.get(4), graveyard.get(5));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard,
                selected.get(0), selected.get(1), selected.get(2));
    }

    @Test
    void opponentsGraveyardDoesNotEnableThreshold() {
        addCreatureReady(player1, new BattlefieldScrounger());
        harness.setGraveyard(player2, List.of(
                new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent scrounger = addCreatureReady(player1, new BattlefieldScrounger());
        List<Card> graveyard = List.of(
                new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote());
        harness.setGraveyard(player1, graveyard);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, graveyard.subList(0, 3).stream()
                .map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scrounger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, scrounger)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, scrounger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scrounger)).isEqualTo(3);
    }

    @Test
    void oncePerTurnRestrictionResetsOnNextTurn() {
        Permanent scrounger = addCreatureReady(player1, new BattlefieldScrounger());
        List<Card> graveyard = List.of(
                new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote());
        harness.setGraveyard(player1, graveyard);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, graveyard.subList(0, 3).stream()
                .map(Card::getId).toList());
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        List<Card> remaining = List.copyOf(gd.playerGraveyards.get(player1.getId()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, remaining.subList(0, 3).stream()
                .map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scrounger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, scrounger)).isEqualTo(6);
    }

    @Test
    void requiresSevenCardsInGraveyard() {
        addCreatureReady(player1, new BattlefieldScrounger());
        harness.setGraveyard(player1, List.of(
                new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote(), new MentalNote(), new MentalNote()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
