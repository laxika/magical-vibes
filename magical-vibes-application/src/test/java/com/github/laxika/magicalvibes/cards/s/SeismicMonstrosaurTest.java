package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeismicMonstrosaur.class, Mountain.class, Forest.class})
class SeismicMonstrosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land draws a card")
    void sacrificingLandDrawsACard() {
        harness.addToBattlefield(player1, new SeismicMonstrosaur());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mountaincycling searches for a Mountain and discards this card")
    void mountaincyclingSearchesForMountain() {
        harness.setHand(player1, List.of(new SeismicMonstrosaur()));
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Seismic Monstrosaur");
        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId()))
                .allMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("The ability cannot be activated without a land to sacrifice")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new SeismicMonstrosaur());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");
    }
}
