package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RingOfThreeWishesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with 3 wish counters when cast")
    void entersWithThreeWishCounters() {
        harness.setHand(player1, List.of(new RingOfThreeWishes()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        var permanents = gd.playerBattlefields.get(player1.getId());
        assertThat(permanents).hasSize(1);
        assertThat(permanents.getFirst().getCounterCount(CounterType.WISH)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating removes a wish counter, taps, and tutors a card to hand")
    void activateTutorsAndRemovesWishCounter() {
        Permanent ring = addReadyRing(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        setupLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();

        String chosenName = search.params().cards().getFirst().getName();
        harness.getGameService().handleInteractionAnswer(gameData, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(ring.isTapped()).isTrue();
        assertThat(ring.getCounterCount(CounterType.WISH)).isEqualTo(2);
        assertThat(gameData.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
    }

    @Test
    @DisplayName("Cannot activate without wish counters")
    void cannotActivateWithoutWishCounters() {
        Permanent ring = addReadyRing(player1);
        ring.setCounterCount(CounterType.WISH, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent ring = addReadyRing(player1);
        ring.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadyRing(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new RingOfThreeWishes());
        perm.setCounterCount(CounterType.WISH, 3);
        perm.setSummoningSick(false);
        return perm;
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Swamp(), new GrizzlyBears(), new GrizzlyBears()));
    }
}
