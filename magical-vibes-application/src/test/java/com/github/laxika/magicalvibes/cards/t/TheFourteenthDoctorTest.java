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

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isFalse();
    }

    private void castDoctor() {
        harness.setHand(player1, List.of(new TheFourteenthDoctor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
    }
}
