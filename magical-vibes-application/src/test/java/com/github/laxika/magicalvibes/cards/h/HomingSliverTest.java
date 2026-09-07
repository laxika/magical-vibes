package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
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

@CardUsed({HomingSliver.class, MetallicSliver.class, GrizzlyBears.class})
class HomingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Homing Sliver grants Slivercycling to Sliver cards in each player's hand")
    void grantsSlivercyclingToOpposingSliverCard() {
        harness.addToBattlefield(player1, new HomingSliver());
        harness.setHand(player2, List.of(new MetallicSliver()));
        harness.setLibrary(player2, List.of(new MetallicSliver(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Metallic Sliver");

        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Metallic Sliver");
        harness.assertInHand(player2, "Metallic Sliver");
    }

    @Test
    @DisplayName("Homing Sliver does not grant Slivercycling to non-Sliver cards")
    void doesNotGrantSlivercyclingToNonSliverCard() {
        harness.addToBattlefield(player1, new HomingSliver());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card has no hand-activated ability");
    }
}
