package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisionarysDance.class, GrizzlyBears.class, Shock.class})
class VisionarysDanceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 3/3 Elemental tokens with flying")
    void createsTwoElementalTokensWithFlying() {
        harness.castFromHand(player1, new VisionarysDance(), "{5}{U}{R}");
        harness.passBothPriorities();

        List<Permanent> elementals = findPermanents(player1, "Elemental");
        assertThat(elementals).hasSize(2);
        assertThat(elementals).allSatisfy(elemental -> {
            assertThat(elemental.getCard().getPower()).isEqualTo(3);
            assertThat(elemental.getCard().getToughness()).isEqualTo(3);
            assertThat(elemental.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("Discards itself and lets you keep one of the top two cards")
    void discardsAndKeepsOneOfTopTwo() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(bears, shock));
        harness.setHand(player1, List.of(new VisionarysDance()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        harness.assertInGraveyard(player1, "Visionary's Dance");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void tokensAreBothBlueAndRed() {
        harness.castFromHand(player1, new VisionarysDance(), "{5}{U}{R}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).hasSize(2).allSatisfy(token ->
                assertThat(token.getCard().getColors())
                        .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED));
    }

    @Test
    void mustChooseOneCardRatherThanPuttingBothIntoGraveyard() {
        Card first = new VisionarysDance();
        Card second = new VisionarysDance();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new VisionarysDance()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
    }

    @Test
    void singleLibraryCardGoesIntoHand() {
        Card onlyCard = new VisionarysDance();
        Card source = new VisionarysDance();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(source));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryStillAllowsActivationWithoutDrawing() {
        Card source = new VisionarysDance();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(source));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playersAttemptedDrawFromEmptyLibrary).isEmpty();
    }
}
