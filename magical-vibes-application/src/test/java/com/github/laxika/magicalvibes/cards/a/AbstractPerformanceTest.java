package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbstractPerformance.class, GrizzlyBears.class, LlanowarElves.class})
class AbstractPerformanceTest extends BaseCardTest {

    @Test
    void opponentChoosesBetweenFixedFaceDownAndFaceUpPiles() {
        List<Card> faceDown = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        List<Card> faceUp = List.of(new LlanowarElves(), new LlanowarElves(), new LlanowarElves(), new LlanowarElves());
        cast(faceDown, faceUp);

        PendingPileSeparation pending = gd.peekPendingInteraction(PendingPileSeparation.class);
        assertThat(pending.pile1Ids()).containsExactlyElementsOf(faceDown.stream().map(Card::getId).toList());
        assertThat(pending.pile2Ids()).containsExactlyElementsOf(faceUp.stream().map(Card::getId).toList());

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.description()).contains("face-down", faceUp.getFirst().getName())
                .doesNotContain(faceDown.getFirst().getName());
    }

    @Test
    void chosenPileGoesToGraveyardAndOtherPileOffersOneFreeSpell() {
        List<Card> faceDown = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        List<Card> faceUp = List.of(new LlanowarElves(), new GrizzlyBears(), new LlanowarElves(), new GrizzlyBears());
        cast(faceDown, faceUp);

        harness.handleMayAbilityChosen(player2, true);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyElementsOf(faceUp);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(faceDown)
                .anyMatch(card -> card instanceof AbstractPerformance);
        assertThat(gd.playerHands.get(player1.getId())).contains(faceUp.get(1), faceUp.get(2), faceUp.get(3));
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(faceUp.getFirst().getId()));

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(faceUp.getFirst().getId()));
    }

    @Test
    void decliningFreeCastPutsTheOtherPileIntoHand() {
        List<Card> faceDown = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        List<Card> faceUp = List.of(new LlanowarElves(), new LlanowarElves(), new LlanowarElves(), new LlanowarElves());
        cast(faceDown, faceUp);

        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(faceUp)
                .anyMatch(card -> card instanceof AbstractPerformance);
        assertThat(gd.playerHands.get(player1.getId())).containsAll(faceDown);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void cast(List<Card> faceDown, List<Card> faceUp) {
        harness.setLibrary(player1, List.of(
                faceDown.get(0), faceDown.get(1), faceDown.get(2), faceDown.get(3),
                faceUp.get(0), faceUp.get(1), faceUp.get(2), faceUp.get(3)));
        harness.setHand(player1, List.of(new AbstractPerformance()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
