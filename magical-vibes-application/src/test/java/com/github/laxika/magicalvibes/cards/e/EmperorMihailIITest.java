package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmperorMihailII.class, CoralMerfolk.class, GrizzlyBears.class})
class EmperorMihailIITest extends BaseCardTest {

    @Test
    @DisplayName("Casts a Merfolk spell from the top of the library and may pay to create a token")
    void castsMerfolkFromTopAndCreatesTokenAfterPayment() {
        harness.addToBattlefield(player1, new EmperorMihailII());
        harness.setLibrary(player1, List.of(new CoralMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        prepareMainPhase();

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Merfolk")).hasSize(1);
        harness.assertOnBattlefield(player1, "Coral Merfolk");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot cast a non-Merfolk card from the top of the library")
    void cannotCastNonMerfolkFromTop() {
        harness.addToBattlefield(player1, new EmperorMihailII());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("Declining the Merfolk trigger payment creates no token")
    void decliningPaymentCreatesNoToken() {
        harness.addToBattlefield(player1, new EmperorMihailII());
        harness.setHand(player1, List.of(new CoralMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Merfolk")).isEmpty();
        harness.assertOnBattlefield(player1, "Coral Merfolk");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
