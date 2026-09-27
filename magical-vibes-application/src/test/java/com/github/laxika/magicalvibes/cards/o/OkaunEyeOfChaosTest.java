package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SorcerersStrongbox;
import com.github.laxika.magicalvibes.cards.z.ZndrspltEyeOfWisdom;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OkaunEyeOfChaos.class, ZndrspltEyeOfWisdom.class, GrizzlyBears.class,
        SorcerersStrongbox.class})
class OkaunEyeOfChaosTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Zndrsplt")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card zndrsplt = namedCard("Zndrsplt, Eye of Wisdom");
        harness.setLibrary(player2, List.of(zndrsplt));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new OkaunEyeOfChaos());
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

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(zndrsplt);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("At the beginning of combat, Okaun flips until a loss and doubles for each win")
    void flipsUntilLossAndDoublesForEachWin() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, cards(50));
        Permanent okaun = addCreatureReady(player1, new OkaunEyeOfChaos());

        advanceToBeginningOfCombat(player1);
        resolveAllTriggers();

        long wins = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("wins the coin flip for Okaun, Eye of Chaos"))
                .count();
        int expectedPower = 3;
        for (int i = 0; i < wins; i++) {
            expectedPower *= 2;
        }
        assertThat(gqs.getEffectivePower(gd, okaun)).isEqualTo(expectedPower);
        assertThat(gqs.getEffectiveToughness(gd, okaun)).isEqualTo(expectedPower);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("loses the coin flip for Okaun, Eye of Chaos"));
    }

    @Test
    @DisplayName("Okaun doubles when another player wins a coin flip")
    void doublesWhenAnotherPlayerWinsCoinFlip() {
        Permanent okaun = addCreatureReady(player1, new OkaunEyeOfChaos());
        harness.addToBattlefield(player2, new SorcerersStrongbox());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        boolean won = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip for Sorcerer's Strongbox"));
        assertThat(gqs.getEffectivePower(gd, okaun)).isEqualTo(won ? 6 : 3);
        assertThat(gqs.getEffectiveToughness(gd, okaun)).isEqualTo(won ? 6 : 3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, okaun)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, okaun)).isEqualTo(3);
    }

    private void advanceToBeginningOfCombat(com.github.laxika.magicalvibes.model.Player activePlayer) {
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
