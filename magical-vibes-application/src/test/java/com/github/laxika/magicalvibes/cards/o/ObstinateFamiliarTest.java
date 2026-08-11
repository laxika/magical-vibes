package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ObstinateFamiliarTest extends BaseCardTest {

    private void resolveDraw() {
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getPlayerInputService().processNextMayAbility(gd);
        });
    }

    private void resolveTwoDraws() {
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getPlayerInputService().processNextMayAbility(gd);
        });
    }

    @Test
    @DisplayName("Skipping a draw leaves the top card on the library")
    void skippingDrawLeavesLibraryUnchanged() {
        harness.addToBattlefield(player1, new ObstinateFamiliar());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Forest");
    }

    @Test
    @DisplayName("Declining the replacement draws normally")
    void decliningDrawsNormally() {
        harness.addToBattlefield(player1, new ObstinateFamiliar());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        resolveDraw();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Each card in a multiple-card draw gets its own choice")
    void eachDrawIsOfferedSeparately() {
        harness.addToBattlefield(player1, new ObstinateFamiliar());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        resolveTwoDraws();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("The replacement does not affect an opponent's draw")
    void opponentDrawsNormally() {
        harness.addToBattlefield(player1, new ObstinateFamiliar());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player2, "Grizzly Bears");
    }
}
