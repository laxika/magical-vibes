package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
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

@CardUsed({EmergentUltimatum.class, EerieUltimatum.class, GrizzlyBears.class,
        LlanowarElves.class, RagingGoblin.class})
class EmergentUltimatumTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to three differently named monocolored cards and offers the rest to cast")
    void searchesChoosesShufflesAndOffersRemainingCards() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card goblin = new RagingGoblin();
        Card multicolored = new EerieUltimatum();
        EmergentUltimatum spell = new EmergentUltimatum();

        harness.setLibrary(player1, List.of(bears, multicolored, elves, goblin));
        harness.setHand(player1, List.of(spell));
        addEmergentUltimatumMana();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.EmergentUltimatumSearchChoice search =
                gd.interaction.activeInteraction(PendingInteraction.EmergentUltimatumSearchChoice.class);
        assertThat(search.pool()).containsExactly(bears, elves, goblin);

        harness.handleMultipleCardsChosen(player1,
                List.of(bears.getId(), elves.getId(), goblin.getId()));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EmergentUltimatumOpponentChoice.class);

        harness.handleMultipleCardsChosen(player2, List.of(bears.getId()));
        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(castChoice.validCardIds()).containsExactlyInAnyOrder(elves.getId(), goblin.getId());

        harness.handleMultipleCardsChosen(player1, List.of(goblin.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(goblin.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(multicolored.getId(), bears.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(elves.getId(), spell.getId());
    }

    @Test
    @DisplayName("Rejects duplicate names in the controller's selection")
    void rejectsDuplicateNames() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new EmergentUltimatum()));
        addEmergentUltimatumMana();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(first.getId(), second.getId())))
                .hasMessageContaining("different names");
    }

    private void addEmergentUltimatumMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
