package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({GuidedPassage.class, GrizzlyBears.class, Forest.class, Shock.class})
class GuidedPassageTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses a creature, land, and noncreature nonland card for the hand")
    void opponentChoosesOneCardOfEachCategory() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card spell = new Shock();
        Card untouched = new Shock();
        castGuidedPassage(List.of(creature, land, spell, untouched));

        PendingInteraction.GuidedPassageChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GuidedPassageChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(creature.getId(), land.getId(), spell.getId(),
                untouched.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(creature.getId(), land.getId(), spell.getId())))
                .hasMessageContaining("Not your turn");

        harness.handleMultipleCardsChosen(player2,
                List.of(creature.getId(), land.getId(), spell.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature, land, spell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Guided Passage");
    }

    @Test
    @DisplayName("An available category must be represented exactly once")
    void rejectsMissingCategory() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card spell = new Shock();
        castGuidedPassage(List.of(creature, land, spell));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2,
                List.of(creature.getId(), land.getId())))
                .hasMessageContaining("noncreature, nonland");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GuidedPassageChoice.class);
    }

    @Test
    @DisplayName("Categories without matching cards are ignored")
    void ignoresMissingCategory() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        castGuidedPassage(List.of(creature, land));

        harness.handleMultipleCardsChosen(player2, List.of(creature.getId(), land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castGuidedPassage(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new GuidedPassage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
