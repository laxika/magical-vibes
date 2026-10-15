package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DakraMystic.class, Forest.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("Accepting with an empty controller library still puts the opponent's revealed card into the graveyard")
    void acceptingWithEmptyControllerLibrary() {
        addReadyDakraMystic();
        Card revealed = new Forest();
        Card next = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(revealed, next));
        activate();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(revealed).doesNotContain(next);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(next);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(revealed, next);
        assertThat(gd.playersAttemptedDrawFromEmptyLibrary).isEmpty();
    }

    @Test
    @DisplayName("Accepting with an empty opponent library still puts the controller's revealed card into the graveyard")
    void acceptingWithEmptyOpponentLibrary() {
        addReadyDakraMystic();
        Card revealed = new GrizzlyBears();
        Card next = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed, next));
        harness.setLibrary(player2, List.of());
        activate();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(revealed).doesNotContain(next);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(revealed, next);
        assertThat(gd.playersAttemptedDrawFromEmptyLibrary).isEmpty();
    }

    @Test
    @DisplayName("Activating taps Dakra Mystic and spends one blue mana")
    void activationPaysManaAndTapCosts() {
        addReadyDakraMystic();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap ability")
    void cannotActivateWhileSummoningSick() {
        Permanent mystic = harness.addToBattlefieldAndReturn(player1, new DakraMystic());
        mystic.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mystic.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
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
