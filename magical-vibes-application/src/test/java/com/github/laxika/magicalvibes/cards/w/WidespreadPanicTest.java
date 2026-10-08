package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChaosWarp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WidespreadPanic.class, Forest.class, TerramorphicExpanse.class, ChaosWarp.class})
class WidespreadPanicTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the player who shuffled put a card from their hand on top of their library")
    void putsShufflingPlayersCardOnTop() {
        Card handCard = new Forest();
        Card oldTop = new WidespreadPanic();
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(oldTop));
        harness.addToBattlefield(player1, new WidespreadPanic());

        shuffleWithExpanse(player2);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class,
                        choice -> {
                            assertThat(choice.playerId()).isEqualTo(player2.getId());
                            assertThat(choice.minCount()).isEqualTo(1);
                            assertThat(choice.maxCount()).isEqualTo(1);
                        });

        harness.handleMultipleCardsChosen(player2, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(handCard, oldTop);
    }

    @Test
    @DisplayName("Also triggers when its controller's ability shuffles their own library")
    void triggersOnOwnShuffle() {
        Card handCard = new Forest();
        Card oldTop = new WidespreadPanic();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(oldTop));
        harness.addToBattlefield(player1, new WidespreadPanic());

        shuffleWithExpanse(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(handCard, oldTop);
    }

    @Test
    @DisplayName("Does nothing when the shuffling player has an empty hand")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new WidespreadPanic()));
        harness.addToBattlefield(player1, new WidespreadPanic());

        shuffleWithExpanse(player2);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The shuffling player chooses exactly one card, and cannot decline")
    void choosesOneCardFromSeveral() {
        Card chosen = new Forest();
        Card kept = new WidespreadPanic();
        harness.setHand(player2, List.of(kept, chosen));
        harness.setLibrary(player2, List.of(new WidespreadPanic()));
        harness.addToBattlefield(player1, new WidespreadPanic());

        shuffleWithExpanse(player2);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(chosen);
    }

    @Test
    @DisplayName("An ability still causes a shuffle and a trigger when the library is empty")
    void emptyLibraryStillTriggers() {
        Card handCard = new Forest();
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new WidespreadPanic());

        shuffleWithExpanse(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player2, List.of(handCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when another player's spell makes an opponent shuffle")
    void anotherPlayersSpellDoesNotTrigger() {
        Card handCard = new Forest();
        harness.setHand(player1, List.of(new ChaosWarp()));
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addToBattlefield(player1, new WidespreadPanic());
        var target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
    }

    @Test
    @DisplayName("Triggers after an opponent's own spell finishes making them shuffle")
    void ownSpellTriggersAfterResolving() {
        Card handCard = new Forest();
        harness.setHand(player2, List.of(new ChaosWarp(), handCard));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addToBattlefield(player1, new WidespreadPanic());
        var target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player2, List.of(handCard.getId()));

        assertThat(gd.playerDecks.get(player2.getId())).startsWith(handCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each copy triggers separately and the second resolves even after the hand empties")
    void multipleCopiesTriggerSeparately() {
        Card handCard = new Forest();
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(new WidespreadPanic()));
        harness.addToBattlefield(player1, new WidespreadPanic());
        harness.addToBattlefield(player1, new WidespreadPanic());

        shuffleWithExpanse(player2);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player2, List.of(handCard.getId()));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(handCard);
    }

    private void shuffleWithExpanse(Player player) {
        harness.addToBattlefield(player, new TerramorphicExpanse());
        int index = gd.playerBattlefields.get(player.getId()).size() - 1;
        harness.activateAbility(player, index, null, null);
        harness.passBothPriorities();
    }
}
