package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BorealOutrider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HorizonSeeker.class, Forest.class, BorealOutrider.class})
class HorizonSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Boast searches for a basic land and puts it into hand")
    void boastSearchesForBasicLand() {
        Permanent seeker = addCreatureReady(player1, new HorizonSeeker());
        seeker.setAttackedThisTurn(true);
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new BorealOutrider()));
        addBoastMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("Boast requires Horizon Seeker to have attacked this turn")
    void boastRequiresThisCreatureToHaveAttacked() {
        addCreatureReady(player1, new HorizonSeeker());
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent seeker = addCreatureReady(player1, new HorizonSeeker());
        seeker.setAttackedThisTurn(true);
        harness.setLibrary(player1, List.of(new BorealOutrider()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("A basic land search may fail to find even when a basic land is available")
    void mayDeclineToFindBasicLand() {
        Permanent seeker = addCreatureReady(player1, new HorizonSeeker());
        seeker.setAttackedThisTurn(true);
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addBoastMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boast resolves normally with an empty library")
    void emptyLibraryDoesNotRequireAChoice() {
        Permanent seeker = addCreatureReady(player1, new HorizonSeeker());
        seeker.setAttackedThisTurn(true);
        harness.setLibrary(player1, List.of());
        addBoastMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An attacked Horizon Seeker may boast while tapped")
    void tappedAttackerCanBoast() {
        Permanent seeker = addCreatureReady(player1, new HorizonSeeker());
        seeker.setAttackedThisTurn(true);
        seeker.tap();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addBoastMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        assertThat(seeker.isTapped()).isTrue();
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals Forest"));
    }

    @Test
    @DisplayName("Boast's activation limit applies before the first activation resolves")
    void cannotBoastAgainWhileFirstActivationIsOnStack() {
        Permanent seeker = addCreatureReady(player1, new HorizonSeeker());
        seeker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.stack).hasSize(1);
    }

    private void addBoastMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
