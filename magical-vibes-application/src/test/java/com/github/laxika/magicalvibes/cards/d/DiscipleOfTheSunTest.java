package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiscipleOfTheSun.class, GrizzlyBears.class, HillGiant.class, Shatter.class})
class DiscipleOfTheSunTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets a permanent card with mana value 3 or less")
    void etbFiltersGraveyardCards() {
        GrizzlyBears eligible = new GrizzlyBears();
        HillGiant tooExpensive = new HillGiant();
        Shatter nonPermanent = new Shatter();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive, nonPermanent));

        castDisciple();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
    }

    @Test
    @DisplayName("ETB returns the chosen permanent card to its owner's hand")
    void returnsChosenPermanentToHand() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castDisciple();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Disciple of the Sun");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does not prompt when no eligible permanent card is in the graveyard")
    void noEligibleTargetSkipsPrompt() {
        harness.setGraveyard(player1, List.of(new HillGiant(), new Shatter()));

        castDisciple();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Disciple of the Sun");
    }

    private void castDisciple() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DiscipleOfTheSun()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
