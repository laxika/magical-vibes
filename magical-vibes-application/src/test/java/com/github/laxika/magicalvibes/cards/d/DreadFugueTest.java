package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadFugue.class, Forest.class, GrizzlyBears.class, ShivanDragon.class})
class DreadFugueTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast can discard any nonland card")
    void normalCastCanDiscardAnyNonlandCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new ShivanDragon(), new Forest())));
        harness.setHand(player1, List.of(new DreadFugue()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Shivan Dragon");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Cleave only allows nonland cards with mana value 2 or less")
    void cleaveRestrictsDiscardToLowManaValueNonlands() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new ShivanDragon(), new GrizzlyBears(), new Forest())));
        harness.setHand(player1, List.of(new DreadFugue()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Shivan Dragon");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Cleave does nothing when the hand has no eligible card")
    void cleaveDoesNothingWhenHandHasNoEligibleCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new ShivanDragon())));
        harness.setHand(player1, List.of(new DreadFugue()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Shivan Dragon");
    }
}
