package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrakhataPillarBug;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelfAssembler.class, ArcboundWorker.class, GrizzlyBears.class, PrakhataPillarBug.class})
class SelfAssemblerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a may search prompt")
    void enteringCreatesMaySearchPrompt() {
        castSelfAssembler();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability offers only Assembly-Worker creature cards")
    void acceptingOffersMatchingCreatureCards() {
        castSelfAssembler();
        setLibrary(new SelfAssembler(), new ArcboundWorker(), new GrizzlyBears());

        resolveEtbMay(true);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName).containsExactly("Self-Assembler");
    }

    @Test
    @DisplayName("Choosing an Assembly-Worker creature puts it into hand")
    void choosingMatchingCreaturePutsItIntoHand() {
        castSelfAssembler();
        setLibrary(new SelfAssembler(), new ArcboundWorker());

        resolveEtbMay(true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Self-Assembler"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the search")
    void decliningSkipsSearch() {
        castSelfAssembler();
        setLibrary(new SelfAssembler());

        resolveEtbMay(false);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void selectedCardIsRevealedAndOnlyOneCopyLeavesLibrary() {
        castSelfAssembler();
        SelfAssembler selected = new SelfAssembler();
        SelfAssembler remaining = new SelfAssembler();
        setLibrary(selected, remaining);

        resolveEtbMay(true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(selected);
        assertThat(gameLogContains("reveals Self-Assembler and puts it into their hand.")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayFailToFindEvenWhenMatchingCardExists() {
        castSelfAssembler();
        SelfAssembler available = new SelfAssembler();
        setLibrary(available);

        resolveEtbMay(true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(available);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void acceptingWithEmptyLibraryCompletesSearch() {
        castSelfAssembler();
        setLibrary();

        resolveEtbMay(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void acceptingWithoutMatchingCardsLeavesLibraryIntact() {
        castSelfAssembler();
        PrakhataPillarBug nonWorker = new PrakhataPillarBug();
        setLibrary(nonWorker);

        resolveEtbMay(true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonWorker);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningPreservesLibraryOrderAndDoesNotShuffle() {
        castSelfAssembler();
        SelfAssembler first = new SelfAssembler();
        SelfAssembler second = new SelfAssembler();
        setLibrary(first, second);

        resolveEtbMay(false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castSelfAssembler() {
        harness.castFromHand(player1, new SelfAssembler(), "{5}");
    }

    private void resolveEtbMay(boolean accept) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
