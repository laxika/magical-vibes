package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFourteenthDoctor.class, GrizzlyBears.class})
class TheFourteenthDoctorTest extends BaseCardTest {

    @Test
    void castTriggerPutsDoctorsInGraveyardAndRestsOnBottom() {
        Card doctor = new TheFourteenthDoctor();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(doctor, bears));
        castDoctor();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(doctor);
        assertThat(gd.playerDecks.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    void mayEnterAsDoctorPutIntoGraveyardFromLibraryAndGainsHasteUntilEndOfTurn() {
        Card doctor = new TheFourteenthDoctor();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(doctor, bears));
        castDoctor();

        harness.passBothPriorities(); // cast trigger
        harness.passBothPriorities(); // creature spell

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(doctor.getId());

        harness.handleMultipleCardsChosen(player1, List.of(doctor.getId()));
        harness.passBothPriorities();

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .findFirst()
                .orElseThrow();
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(doctor);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isFalse();
    }

    @Test
    void copyingGrantsHasteImmediatelyWithoutAnEtbTrigger() {
        Card doctor = new TheFourteenthDoctor();
        harness.setLibrary(player1, List.of(doctor, new GrizzlyBears()));
        castDoctor();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(doctor.getId()));

        Permanent entered = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayDeclineCopyAndEnterWithoutHaste() {
        Card doctor = new TheFourteenthDoctor();
        harness.setLibrary(player1, List.of(doctor, new GrizzlyBears()));
        castDoctor();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(doctor);
    }

    @Test
    void cannotCopyDoctorAlreadyInGraveyardOrInOpponentsGraveyard() {
        Card ownDoctor = new TheFourteenthDoctor();
        Card opposingDoctor = new TheFourteenthDoctor();
        harness.setGraveyard(player1, List.of(ownDoctor));
        harness.setGraveyard(player2, List.of(opposingDoctor));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castDoctor();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gqs.hasKeyword(gd, gd.playerBattlefields.get(player1.getId()).getFirst(), Keyword.HASTE))
                .isFalse();
    }

    @Test
    void onlyRevealsFourteenCardsAndPutsOthersBelowUnrevealedCards() {
        Card revealedDoctor = new TheFourteenthDoctor();
        List<Card> revealedBears = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            revealedBears.add(new GrizzlyBears());
        }
        Card unrevealedDoctor = new TheFourteenthDoctor();
        List<Card> library = new ArrayList<>();
        library.add(revealedDoctor);
        library.addAll(revealedBears);
        library.add(unrevealedDoctor);
        harness.setLibrary(player1, library);
        castDoctor();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(revealedDoctor);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(14);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unrevealedDoctor);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 14))
                .containsExactlyInAnyOrderElementsOf(revealedBears);
    }

    private void castDoctor() {
        harness.setHand(player1, List.of(new TheFourteenthDoctor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
    }
}
