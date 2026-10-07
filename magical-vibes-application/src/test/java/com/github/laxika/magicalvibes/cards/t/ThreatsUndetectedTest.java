package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.e.ElfhameWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Snarespinner;
import com.github.laxika.magicalvibes.cards.u.UrborgLhurgoyf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreatsUndetected.class, AirElemental.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, LlanowarElves.class, CosisTrickster.class, ElfhameWurm.class,
        Snarespinner.class, UrborgLhurgoyf.class})
class ThreatsUndetectedTest extends BaseCardTest {

    @Test
    void searchesCreaturesWithDifferentPowersAndShufflesChosenCardsBack() {
        Card onePower = new LlanowarElves();
        Card duplicatePower = new LlanowarElves();
        Card twoPower = new GrizzlyBears();
        Card threePower = new HillGiant();
        Card fourPower = new AirElemental();
        Card nonCreature = new Forest();
        harness.setLibrary(player1, List.of(onePower, duplicatePower, twoPower, threePower, fourPower, nonCreature));
        harness.setHand(player1, List.of(new ThreatsUndetected()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(offeredCards()).containsExactlyInAnyOrder(onePower, duplicatePower, twoPower,
                threePower, fourPower);
        pick(onePower);
        assertThat(offeredCards()).doesNotContain(duplicatePower);

        pick(twoPower);
        pick(threePower);
        pick(fourPower);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player2, List.of(onePower.getId(), threePower.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(twoPower, fourPower);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(duplicatePower, nonCreature, onePower, threePower);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Threats Undetected");
    }

    private List<Card> offeredCards() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards();
    }

    private void pick(Card card) {
        int index = offeredCards().indexOf(card);
        assertThat(index).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void mayStopSearchingBeforeFourCards(int count) {
        Card one = new LlanowarElves();
        Card two = new GrizzlyBears();
        Card three = new HillGiant();
        Card four = new AirElemental();
        List<Card> creatures = List.of(one, two, three, four);
        harness.setLibrary(player1, creatures);
        castThreats();
        for (int i = 0; i < count; i++) {
            pick(creatures.get(i));
        }
        harness.handleCardChosen(player1, -1);
        if (count > 0) {
            PendingInteraction.MultiGraveyardChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
            assertThat(choice.playerId()).isEqualTo(player2.getId());
            assertThat(choice.minCount()).isEqualTo(Math.min(2, count));
            assertThat(choice.maxCount()).isEqualTo(Math.min(2, count));
            harness.handleMultipleCardsChosen(player2,
                    creatures.subList(0, Math.min(2, count)).stream().map(Card::getId).toList());
        }
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyElementsOf(creatures.subList(Math.min(2, count), count));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(
                creatures.stream().filter(c -> count <= 2 || c != three).toList());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Threats Undetected");
    }

    @Test
    void libraryWithoutCreaturesFinishesWithoutAChoice() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        castThreats();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Threats Undetected");
    }

    @Test
    void comparesCharacteristicDefinedPowerInTheLibrary() {
        Card lhurgoyf = new UrborgLhurgoyf();
        Card spider = new Snarespinner();
        Card wurm = new ElfhameWurm();
        harness.setGraveyard(player1, List.of(new ElfhameWurm()));
        harness.setLibrary(player1, List.of(lhurgoyf, spider, wurm));
        castThreats();
        pick(spider);
        assertThat(offeredCards()).containsExactly(wurm);
    }

    @Test
    void shufflesOnlyOnceAfterTheOpponentChooses() {
        harness.addToBattlefield(player2, new CosisTrickster());
        Card one = new LlanowarElves();
        Card two = new GrizzlyBears();
        Card three = new HillGiant();
        harness.setLibrary(player1, List.of(one, two, three));
        castThreats();
        pick(one);
        pick(two);
        pick(three);
        harness.handleMultipleCardsChosen(player2, List.of(one.getId(), two.getId()));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(CosisTrickster.class);
    }

    @Test
    void opponentPromptDescribesShufflingChosenCardsIntoTheLibrary() {
        Card one = new LlanowarElves();
        Card two = new GrizzlyBears();
        harness.setLibrary(player1, List.of(one, two));
        castThreats();
        pick(one);
        pick(two);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.prompt()).containsIgnoringCase("library").doesNotContain("graveyard");
    }

    private void castThreats() {
        harness.setHand(player1, List.of(new ThreatsUndetected()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
