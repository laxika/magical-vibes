package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EcologicalAppreciationTest extends BaseCardTest {

    @Test
    @DisplayName("Controller searches library and graveyard, then opponent divides four revealed creatures")
    void searchesBothZonesAndOpponentChoosesTwo() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card goblin = new RagingGoblin();
        Card ornithopter = new Ornithopter();
        Card spell = new EcologicalAppreciation();
        setUpAndCast(spell, 2, List.of(bears, elves, goblin), List.of(ornithopter));

        PendingInteraction.EcologicalAppreciationSearchChoice search =
                gd.interaction.activeInteraction(PendingInteraction.EcologicalAppreciationSearchChoice.class);
        assertThat(search.pool()).containsExactly(bears, elves, goblin, ornithopter);

        harness.handleMultipleCardsChosen(player1,
                List.of(bears.getId(), elves.getId(), goblin.getId(), ornithopter.getId()));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EcologicalAppreciationOpponentChoice.class);

        harness.handleMultipleCardsChosen(player2, List.of(bears.getId(), goblin.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(bears.getId(), goblin.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(elves.getId(), ornithopter.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("One or two found cards are all shuffled into the library")
    void oneOrTwoFoundCardsDoNotEnterBattlefield() {
        Card goblin = new RagingGoblin();
        Card elves = new LlanowarElves();
        setUpAndCast(new EcologicalAppreciation(), 1, List.of(goblin, new Shock()), List.of(elves));

        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.EcologicalAppreciationSearchChoice.class).pool())
                .containsExactly(goblin, elves);

        harness.handleMultipleCardsChosen(player1, List.of(goblin.getId(), elves.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(goblin.getId(), elves.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller cannot choose two creatures with the same name")
    void selectedNamesMustBeDifferent() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        setUpAndCast(new EcologicalAppreciation(), 2, List.of(first, second), List.of());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(first.getId(), second.getId())))
                .hasMessageContaining("different names");
    }

    private void setUpAndCast(Card spell, int xValue, List<Card> library, List<Card> graveyard) {
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, xValue + 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }
}
