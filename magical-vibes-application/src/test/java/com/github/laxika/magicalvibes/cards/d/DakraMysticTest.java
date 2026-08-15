package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DakraMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals the top card of each player's library and prompts the controller")
    void revealsTopCardsAndPromptsController() {
        addReadyDakraMystic();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));
        activate();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Accepting puts every revealed card into its owner's graveyard")
    void acceptingPutsRevealedCardsIntoGraveyards() {
        addReadyDakraMystic();
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(bears));
        harness.setLibrary(player2, List.of(forest));
        activate();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("Declining makes each player draw their revealed card")
    void decliningMakesEachPlayerDrawRevealedCard() {
        addReadyDakraMystic();
        Card revealedBears = new GrizzlyBears();
        Card revealedForest = new Forest();
        Card nextBears = new GrizzlyBears();
        Card nextForest = new Forest();
        harness.setLibrary(player1, List.of(revealedBears, nextBears));
        harness.setLibrary(player2, List.of(revealedForest, nextForest));
        activate();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(revealedBears).doesNotContain(nextBears);
        assertThat(gd.playerHands.get(player2.getId())).contains(revealedForest).doesNotContain(nextForest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextBears);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextForest);
    }

    private void activate() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void addReadyDakraMystic() {
        Permanent dakraMystic = harness.addToBattlefieldAndReturn(player1, new DakraMystic());
        dakraMystic.setSummoningSick(false);
    }
}
