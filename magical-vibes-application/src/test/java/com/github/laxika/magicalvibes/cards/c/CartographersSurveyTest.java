package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CartographersSurvey.class, Forest.class, Shock.class})
class CartographersSurveyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts up to two revealed lands onto the battlefield tapped")
    void putsUpToTwoLandsOntoBattlefieldTapped() {
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        setLibrary(forest1, new Shock(), forest2, new Shock(), new Shock(), new Shock(), new Shock());

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest1.getId(), forest2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.randomRemainingToBottom()).isTrue();
        assertThat(choice.selectedToBattlefieldTapped()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(forest1.getId(), forest2.getId()));

        assertThat(permanentFor(forest1).isTapped()).isTrue();
        assertThat(permanentFor(forest2).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May put only one revealed land onto the battlefield")
    void mayPutOnlyOneLand() {
        Card forest = new Forest();
        setLibrary(forest, new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(permanentFor(forest).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Puts all seven cards on the bottom when no land is revealed")
    void noLandLeavesLibraryIntact() {
        setLibrary(new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new CartographersSurvey()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private Permanent permanentFor(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
