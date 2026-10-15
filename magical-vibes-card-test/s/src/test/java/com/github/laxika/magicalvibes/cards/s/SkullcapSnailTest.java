package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.cards.d.DeepCavernBat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkullcapSnail.class, DeepCavernBat.class, Swamp.class, DeadWeight.class})
class SkullcapSnailTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts the target opponent to choose a card from their hand")
    void etbPromptsTargetOpponentToChooseCard() {
        harness.setHand(player2, List.of(new DeepCavernBat(), new DeepCavernBat()));

        castAndResolve();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Target opponent exiles the card they choose")
    void targetOpponentExilesChosenCard() {
        DeepCavernBat remaining = new DeepCavernBat();
        DeepCavernBat chosen = new DeepCavernBat();
        harness.setHand(player2, List.of(remaining, chosen));

        castAndResolve();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new SkullcapSnail()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("An opponent with an empty hand has nothing to exile")
    void emptyHandDoesNotPromptForChoice() {
        harness.setHand(player2, List.of());

        castAndResolve();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The opponent may choose a land and exiles their only card")
    void opponentCanExileTheirOnlyCardEvenIfItIsALand() {
        Swamp land = new Swamp();
        harness.setHand(player2, List.of(land));

        castAndResolve();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Exiled cards stay exiled when Skullcap Snail leaves the battlefield")
    void exiledCardDoesNotReturnWhenSnailDies() {
        DeepCavernBat chosen = new DeepCavernBat();
        harness.setHand(player2, List.of(chosen));
        castAndResolve();
        harness.handleCardChosen(player2, 0);
        var snail = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player1, List.of(new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, snail.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(snail);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(snail.getCard());
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new SkullcapSnail()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();
    }
}
