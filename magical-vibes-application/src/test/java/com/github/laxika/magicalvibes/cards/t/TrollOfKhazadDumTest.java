package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrollOfKhazadDum.class, Swamp.class})
class TrollOfKhazadDumTest extends BaseCardTest {

    @Test
    @DisplayName("Troll of Khazad-dum can't be blocked by fewer than three creatures")
    void cannotBeBlockedByFewerThanThreeCreatures() {
        Permanent troll = addCreatureReady(player1, new TrollOfKhazadDum());
        addCreatureReady(player2, new TrollOfKhazadDum());
        troll.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by 3 or more creatures");
    }

    @Test
    @DisplayName("Swampcycling discards Troll of Khazad-dum and searches for a Swamp")
    void swampcyclingSearchesForSwamp() {
        TrollOfKhazadDum troll = new TrollOfKhazadDum();
        harness.setHand(player1, List.of(troll));
        harness.setLibrary(player1, List.of(new TrollOfKhazadDum(), new Swamp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Troll of Khazad-dûm");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Troll of Khazad-dûm");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Swamp");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Swamp");
    }

    @Test
    void cannotBeBlockedByTwoCreatures() {
        Permanent troll = addCreatureReady(player1, new TrollOfKhazadDum());
        addCreatureReady(player2, new TrollOfKhazadDum());
        addCreatureReady(player2, new TrollOfKhazadDum());
        troll.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by 3 or more creatures");
    }

    @Test
    void canBeBlockedByThreeCreatures() {
        Permanent troll = addCreatureReady(player1, new TrollOfKhazadDum());
        addCreatureReady(player2, new TrollOfKhazadDum());
        addCreatureReady(player2, new TrollOfKhazadDum());
        addCreatureReady(player2, new TrollOfKhazadDum());
        troll.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0), new BlockerAssignment(2, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void canRemainUnblocked() {
        Permanent troll = addCreatureReady(player1, new TrollOfKhazadDum());
        addCreatureReady(player2, new TrollOfKhazadDum());
        troll.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void swampcyclingCanFailToFindEvenWithSwampAvailable() {
        Swamp swamp = new Swamp();
        harness.setHand(player1, List.of(new TrollOfKhazadDum()));
        harness.setLibrary(player1, List.of(swamp));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Troll of Khazad-dûm");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(swamp);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void swampcyclingResolvesWithoutMatchingCard() {
        TrollOfKhazadDum otherTroll = new TrollOfKhazadDum();
        harness.setHand(player1, List.of(new TrollOfKhazadDum()));
        harness.setLibrary(player1, List.of(otherTroll));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Troll of Khazad-dûm");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherTroll);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
