package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemogorgonsClutches.class, Forest.class, GrizzlyBears.class, Island.class, Peek.class})
class DemogorgonsClutchesTest extends BaseCardTest {

    @Test
    void targetOpponentDiscardsMillsAndLosesLife() {
        Card discardedCreature = new GrizzlyBears();
        Card discardedSpell = new Peek();
        Card remainingHandCard = new Forest();
        Card milledFirst = new Island();
        Card milledSecond = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(discardedCreature, discardedSpell, remainingHandCard)));
        harness.setLibrary(player2, List.of(milledFirst, milledSecond));
        harness.setHand(player1, List.of(new DemogorgonsClutches()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int opponentLife = gd.getLife(player2.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingHandCard);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(discardedCreature, discardedSpell, milledFirst, milledSecond);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void cannotTargetController() {
        harness.setHand(player1, List.of(new DemogorgonsClutches()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "0, 1", "0, 3", "1, 0", "1, 1", "1, 3", "2, 2"})
    void resolvesRemainingInstructionsWhenHandOrLibraryIsShort(int handSize, int librarySize) {
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < handSize; i++) {
            hand.add(new Forest());
        }
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < librarySize; i++) {
            library.add(new Island());
        }
        harness.setHand(player2, new ArrayList<>(hand));
        harness.setLibrary(player2, library);
        harness.setHand(player1, List.of(new DemogorgonsClutches()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        if (handSize > 0) {
            assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
            assertThat(gd.getLife(player2.getId())).isEqualTo(20);
            for (int i = 0; i < handSize; i++) {
                harness.handleCardChosen(player2, 0);
            }
        }

        int milledCount = Math.min(2, librarySize);
        List<Card> expectedGraveyard = new ArrayList<>(hand);
        expectedGraveyard.addAll(library.subList(0, milledCount));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrderElementsOf(expectedGraveyard);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyElementsOf(library.subList(milledCount, librarySize));
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
