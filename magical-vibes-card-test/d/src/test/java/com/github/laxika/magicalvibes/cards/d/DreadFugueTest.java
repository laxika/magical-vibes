package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BloodFountain;
import com.github.laxika.magicalvibes.cards.r.RotTideGargantua;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadFugue.class, Forest.class, DoomedDissenter.class, RotTideGargantua.class, BloodFountain.class})
class DreadFugueTest extends BaseCardTest {

    @Test
    @DisplayName("Cleave can discard any nonland card but cannot discard a land")
    void cleaveCanDiscardAnyNonlandCard() {
        harness.setHand(player2, List.of(new RotTideGargantua(), new DoomedDissenter(), new Forest()));
        harness.setHand(player1, List.of(new DreadFugue()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Rot-Tide Gargantua");
        harness.assertInHand(player2, "Doomed Dissenter");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Normal cast only allows nonland cards with mana value 2 or less")
    void normalCastRestrictsDiscardToLowManaValueNonlands() {
        harness.setHand(player2, List.of(
                new RotTideGargantua(), new DoomedDissenter(), new Forest()));
        harness.setHand(player1, List.of(new DreadFugue()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Doomed Dissenter");
        harness.assertInHand(player2, "Rot-Tide Gargantua");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Normal cast does nothing when the hand has no eligible card")
    void normalCastDoesNothingWhenHandHasNoEligibleCard() {
        harness.setHand(player2, List.of(new RotTideGargantua(), new Forest()));
        harness.setHand(player1, List.of(new DreadFugue()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Rot-Tide Gargantua");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Cleave does not discard from a hand containing only lands")
    void cleaveDoesNothingWhenHandContainsOnlyLands() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new DreadFugue()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Normal cast resolves against an empty hand without a choice")
    void normalCastResolvesAgainstEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DreadFugue()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Dread Fugue");
    }

    @Test
    @DisplayName("Normal cast may target its controller and discard a low mana value card")
    void normalCastCanTargetController() {
        harness.setHand(player1, List.of(new DreadFugue(), new DoomedDissenter(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Doomed Dissenter");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Normal cast can discard a low mana value noncreature card")
    void normalCastCanDiscardNoncreature() {
        harness.setHand(player2, List.of(new BloodFountain(), new Forest()));
        harness.setHand(player1, List.of(new DreadFugue()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Blood Fountain");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Cleave resolves against an empty hand without a choice")
    void cleaveResolvesAgainstEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DreadFugue()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Dread Fugue");
    }
}
