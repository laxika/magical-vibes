package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WordsOfWind.class, GrizzlyBears.class, Island.class})
class WordsOfWindTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses a permanent before the selected permanents return")
    void eachPlayerChoosesPermanentBeforeReturningThem() {
        harness.addToBattlefield(player1, new WordsOfWind());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        Permanent wordsOfWind = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent player1Bears = gd.playerBattlefields.get(player1.getId()).get(1);
        Permanent player1Island = gd.playerBattlefields.get(player1.getId()).get(2);
        Permanent player2Bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        Permanent player2Island = gd.playerBattlefields.get(player2.getId()).get(1);

        activateWordsOfWind();
        draw(player1);

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactly(
                wordsOfWind.getId(), player1Bears.getId(), player1Island.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player1Island.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactly(player2Bears.getId(), player2Island.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(wordsOfWind, player1Bears, player1Island);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(player2Bears, player2Island);

        harness.handleMultiplePermanentsChosen(player2, List.of(player2Bears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(wordsOfWind, player1Bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(player2Island);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Island.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Bears.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        draw(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Island.getCard(), drawnCard);
    }

    @Test
    @DisplayName("A replacement with no choice still replaces the draw")
    void noChoiceStillReplacesDraw() {
        harness.addToBattlefield(player1, new WordsOfWind());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent wordsOfWind = gd.playerBattlefields.get(player1.getId()).getFirst();

        activateWordsOfWind();
        draw(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wordsOfWind.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void activateWordsOfWind() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void draw(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
