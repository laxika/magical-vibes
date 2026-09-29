package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LunarHatchling.class, Forest.class, GrizzlyBears.class})
class LunarHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Basic landcycling searches for a basic land")
    void basicLandcyclingSearchesForBasicLand() {
        harness.setHand(player1, List.of(new LunarHatchling()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lunar Hatchling");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).singleElement()
                .satisfies(card -> assertThat(card.getName()).isEqualTo("Forest"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Escape exiles a land and five other graveyard cards")
    void escapeExilesLandAndOtherGraveyardCards() {
        Card lunarHatchling = new LunarHatchling();
        List<Card> otherCards = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, List.of(lunarHatchling, otherCards.get(0), otherCards.get(1), otherCards.get(2),
                otherCards.get(3), otherCards.get(4)));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(), List.of(1, 2, 3, 4, 5), null,
                List.of(), null, forest.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(forest.getCard(), otherCards.get(0), otherCards.get(1), otherCards.get(2),
                        otherCards.get(3), otherCards.get(4));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(lunarHatchling.getId()));
    }

    @Test
    @DisplayName("Escape requires a land to exile")
    void escapeRequiresLandToExile() {
        harness.setGraveyard(player1, List.of(new LunarHatchling(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(),
                List.of(1, 2, 3, 4, 5), null, List.of(), null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
