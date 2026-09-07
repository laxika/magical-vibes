package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FreestriderLookout.class, Forest.class, Shock.class})
class FreestriderLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Crime trigger may put a land from the top five onto the battlefield tapped")
    void crimeTriggerPutsChosenLandOntoBattlefieldTapped() {
        FreestriderLookout lookout = new FreestriderLookout();
        Forest forest = new Forest();
        setLibrary(new Shock(), forest, new Shock(), new Shock(), new Shock());
        harness.addToBattlefield(player1, lookout);

        commitCrime();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        Permanent enteredForest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest)
                .findFirst()
                .orElseThrow();
        assertThat(enteredForest.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).doesNotContain(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Crime trigger puts all five cards on the bottom when no land is chosen")
    void crimeTriggerDoesNothingWithoutLand() {
        setLibrary(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        harness.addToBattlefield(player1, new FreestriderLookout());

        commitCrime();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Crime trigger fires only once each turn")
    void crimeTriggerFiresOnlyOnceEachTurn() {
        setLibrary(new Forest(), new Forest(), new Shock(), new Shock(), new Shock());
        harness.addToBattlefield(player1, new FreestriderLookout());

        commitCrime();
        harness.handleMultipleCardsChosen(player1,
                List.of(((PendingInteraction.LibraryRevealChoice) gd.interaction.activeInteraction()).validCardIds().getFirst()));

        commitCrime();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void commitCrime() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
