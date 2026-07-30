package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrimgrinCorpseBorn;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppetiteForBrainsTest extends BaseCardTest {

    @Test
    @DisplayName("Caster chooses a card with mana value 4 or greater and it is exiled")
    void choosingExpensiveCardExilesIt() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrimgrinCorpseBorn(), new GrizzlyBears())));

        harness.setHand(player1, List.of(new AppetiteForBrains()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grimgrin, Corpse-Born"));
        harness.assertNotInGraveyard(player2, "Grimgrin, Corpse-Born");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Cards with mana value 3 or less are not choosable")
    void cheapCardsExcluded() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrimgrinCorpseBorn())));

        harness.setHand(player1, List.of(new AppetiteForBrains()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Grizzly Bears (mana value 2) is excluded; only Grimgrin (mana value 5) is valid.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Hand without a card of mana value 4 or greater yields no choice")
    void noExpensiveCardNoChoice() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        harness.setHand(player1, List.of(new AppetiteForBrains()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target self — must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new AppetiteForBrains(), new GrimgrinCorpseBorn())));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
