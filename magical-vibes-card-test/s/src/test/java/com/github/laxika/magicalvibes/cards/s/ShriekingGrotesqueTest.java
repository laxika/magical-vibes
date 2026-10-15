package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ShriekingGrotesque.class)
class ShriekingGrotesqueTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the target player discard when black mana was spent to cast it")
    void discardsWhenBlackManaWasSpent() {
        harness.setHand(player2, List.of(new ShriekingGrotesque()));
        castShriekingGrotesque(player2.getId(), ManaColor.BLACK, 2);

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Shrieking Grotesque");
    }

    @Test
    @DisplayName("Does not trigger when no black mana was spent to cast it")
    void doesNotTriggerWithoutBlackMana() {
        harness.setHand(player2, List.of(new ShriekingGrotesque()));
        castShriekingGrotesque(player2.getId(), ManaColor.WHITE, 3);

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers when one black mana pays part of the generic cost")
    void triggersWhenBlackManaPaysGenericCost() {
        harness.setHand(player1, List.of(new ShriekingGrotesque()));
        harness.setHand(player2, List.of(new ShriekingGrotesque()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, player2.getId());

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Shrieking Grotesque");
    }

    @Test
    @DisplayName("Does nothing when the target player has no cards")
    void doesNothingForEmptyTargetHand() {
        harness.setHand(player2, List.of());
        castShriekingGrotesque(player2.getId(), ManaColor.BLACK, 2);

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetItsController() {
        harness.setHand(player1, List.of(new ShriekingGrotesque(), new ShriekingGrotesque()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, player1.getId());

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shrieking Grotesque");
    }

    @Test
    @DisplayName("Does not trigger when it enters without being cast, even with black mana available")
    void doesNotTriggerWhenPutOntoBattlefield() {
        harness.setHand(player2, List.of(new ShriekingGrotesque()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.enterBattlefieldAndReturn(player1, new ShriekingGrotesque());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Shrieking Grotesque");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The target player chooses exactly one card to discard")
    void targetPlayerChoosesOneCardFromMultipleCards() {
        ShriekingGrotesque keptCard = new ShriekingGrotesque();
        ShriekingGrotesque discardedCard = new ShriekingGrotesque();
        harness.setHand(player2, List.of(keptCard, discardedCard));
        castShriekingGrotesque(player2.getId(), ManaColor.BLACK, 2);

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castShriekingGrotesque(java.util.UUID targetPlayerId, ManaColor manaColor, int amount) {
        harness.setHand(player1, List.of(new ShriekingGrotesque()));
        if (manaColor == ManaColor.BLACK) {
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.BLACK, amount);
        } else {
            harness.addMana(player1, ManaColor.WHITE, amount);
        }
        harness.castCreature(player1, 0, targetPlayerId);
    }
}
