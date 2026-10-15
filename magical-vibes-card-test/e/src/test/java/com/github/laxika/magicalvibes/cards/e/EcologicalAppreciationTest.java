package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.OppositionAgent;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({EcologicalAppreciation.class, GrizzlyBears.class, LlanowarElves.class,
        Ornithopter.class, RagingGoblin.class, Shock.class, AvenMindcensor.class, OppositionAgent.class})
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
        Card shock = new Shock();
        setUpAndCast(new EcologicalAppreciation(), 1, List.of(goblin, shock), List.of(elves));

        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.EcologicalAppreciationSearchChoice.class).pool())
                .containsExactly(goblin, elves);

        harness.handleMultipleCardsChosen(player1, List.of(goblin.getId(), elves.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(goblin.getId(), elves.getId(), shock.getId());
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

    @Test
    @DisplayName("Finding three creatures puts only the creature not chosen by the opponent onto the battlefield")
    void threeFoundCardsPutOneOntoBattlefield() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card goblin = new RagingGoblin();
        Card spell = new EcologicalAppreciation();
        setUpAndCast(spell, 2, List.of(bears, elves), List.of(goblin));

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId(), goblin.getId()));
        harness.handleMultipleCardsChosen(player2, List.of(bears.getId(), elves.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bears, elves);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(goblin.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("The controller may choose zero cards even when qualifying creatures exist")
    void mayChooseZeroCards() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card spell = new EcologicalAppreciation();
        setUpAndCast(spell, 2, List.of(bears), List.of(elves));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(elves);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("X zero allows a zero-mana creature but excludes positive-mana creatures in both zones")
    void zeroXFindsOnlyZeroManaCreatures() {
        Card ornithopter = new Ornithopter();
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card spell = new EcologicalAppreciation();
        setUpAndCast(spell, 0, List.of(ornithopter, bears), List.of(elves));

        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.EcologicalAppreciationSearchChoice.class).pool())
                .containsExactly(ornithopter);
        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(ornithopter, bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(elves);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("A search with no qualifying creatures still exiles the spell")
    void noQualifyingCreaturesStillExilesSpell() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card spell = new EcologicalAppreciation();
        setUpAndCast(spell, 0, List.of(shock), List.of(bears));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("An opposing Aven Mindcensor limits only the library portion of the search to its top four cards")
    void mindcensorLimitsLibrarySearchButNotGraveyard() {
        harness.addToBattlefield(player2, new AvenMindcensor());
        Card topCreature = new LlanowarElves();
        Card fifthCard = new GrizzlyBears();
        Card graveyardCreature = new Ornithopter();
        setUpAndCast(new EcologicalAppreciation(), 2,
                List.of(topCreature, new Shock(), new Shock(), new Shock(), fifthCard),
                List.of(graveyardCreature));

        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.EcologicalAppreciationSearchChoice.class).pool())
                .containsExactly(topCreature, graveyardCreature);
    }

    @Test
    @DisplayName("Opposition Agent controls the library search and exiles found cards instead of shuffling them")
    void oppositionAgentControlsSearchAndExilesFoundCards() {
        harness.addToBattlefield(player2, new OppositionAgent());
        Card bears = new GrizzlyBears();
        Card spell = new EcologicalAppreciation();
        setUpAndCast(spell, 2, List.of(bears), List.of());

        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player2.getId());
        harness.handleMultipleCardsChosen(player2, List.of(bears.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears, spell);
        assertThat(gd.exilePlayPermissions.get(bears.getId())).isEqualTo(player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void setUpAndCast(Card spell, int xValue, List<Card> library, List<Card> graveyard) {
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, xValue + 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }
}
