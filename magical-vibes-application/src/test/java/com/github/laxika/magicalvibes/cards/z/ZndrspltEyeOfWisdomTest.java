package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SorcerersStrongbox;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZndrspltEyeOfWisdom.class, GrizzlyBears.class, SorcerersStrongbox.class})
class ZndrspltEyeOfWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Okaun")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card okaun = namedCard("Okaun, Eye of Chaos");
        harness.setLibrary(player2, List.of(okaun));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new ZndrspltEyeOfWisdom());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(okaun);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("At the beginning of combat, Zndrsplt flips until a loss and draws for each win")
    void flipsUntilLossAtBeginningOfCombat() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, cards(50));
        harness.addToBattlefield(player1, new ZndrspltEyeOfWisdom());

        advanceToBeginningOfCombat(player1);
        resolveAllTriggers();

        long wins = gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains("wins the coin flip for Zndrsplt, Eye of Wisdom"))
                .count();
        long losses = gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains("loses the coin flip for Zndrsplt, Eye of Wisdom"))
                .count();
        assertThat(losses).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize((int) wins);
    }

    @Test
    @DisplayName("Zndrsplt draws when another player wins a coin flip")
    void drawsWhenAnotherPlayerWinsCoinFlip() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new ZndrspltEyeOfWisdom());
        harness.addToBattlefield(player2, new SorcerersStrongbox());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        boolean won = gameLogContains("wins the coin flip for Sorcerer's Strongbox");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(won ? 1 : 0);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private List<Card> cards(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> (Card) new GrizzlyBears())
                .toList();
    }

    private Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
