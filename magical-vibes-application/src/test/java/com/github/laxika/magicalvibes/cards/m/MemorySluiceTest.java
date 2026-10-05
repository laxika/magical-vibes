package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.s.SickleRipper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MemorySluice.class, BriarberryCohort.class, SickleRipper.class})
class MemorySluiceTest extends BaseCardTest {

    @Test
    @DisplayName("Mills four cards from the top of the target player's library")
    void millsFourCards() {
        harness.setHand(player1, List.of(new MemorySluice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        harness.setLibrary(player1, deck.subList(deck.size() - 10, deck.size()));
        Card topCard = gd.playerDecks.get(player2.getId()).getFirst();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new MemorySluice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        harness.setLibrary(player2, deck.subList(deck.size() - 10, deck.size()));

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        // 4 milled cards + Memory Sluice itself after resolving
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Conspire taps two color-sharing creatures and queues a copy of the spell")
    void conspireTapsCreaturesAndQueuesCopy() {
        harness.setHand(player1, List.of(new MemorySluice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent zombie1 = addCreatureReady(player1, new SickleRipper());
        Permanent zombie2 = addCreatureReady(player1, new SickleRipper());

        harness.castWithConspire(player1, 0, player2.getId(), List.of(zombie1.getId(), zombie2.getId()));

        assertThat(zombie1.isTapped()).isTrue();
        assertThat(zombie2.isTapped()).isTrue();

        // The spell plus one conspire copy trigger are on the stack.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack).anyMatch(e -> e.getEffectsToResolve().stream()
                .anyMatch(fx -> fx instanceof CopyControllerCastSpellEffect));
    }

    @Test
    void millsAllRemainingCardsFromShortLibrary() {
        List<Card> cards = List.of(new SickleRipper(), new BriarberryCohort());
        harness.setLibrary(player2, cards);
        harness.setHand(player1, List.of(new MemorySluice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrderElementsOf(cards);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesAgainstEmptyLibrary() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new MemorySluice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void conspireWithDifferentColorsAndSummoningSicknessMillsEight() {
        harness.setHand(player1, List.of(new MemorySluice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());
        Permanent black = harness.addToBattlefieldAndReturn(player1, new SickleRipper());
        int initialLibrarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castWithConspire(player1, 0, player2.getId(), List.of(blue.getId(), black.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        harness.passBothPriorities();

        assertThat(blue.isTapped()).isTrue();
        assertThat(black.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(initialLibrarySize - 8);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void conspireCopyCanTargetDifferentPlayer() {
        harness.setHand(player1, List.of(new MemorySluice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent first = addCreatureReady(player1, new BriarberryCohort());
        Permanent second = addCreatureReady(player1, new SickleRipper());
        int ownLibrarySize = gd.playerDecks.get(player1.getId()).size();
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castWithConspire(player1, 0, player2.getId(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(ownLibrarySize - 4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize - 4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }
}
