package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Forest.class, JalumTome.class})
class JalumTomeTest extends BaseCardTest {

    @Test
    @DisplayName("The ability cannot be activated with only one mana")
    void cannotActivateWithoutTwoMana() {
        Permanent tome = harness.addToBattlefieldAndReturn(player1, new JalumTome());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tome.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Tome cannot activate even with enough mana")
    void cannotActivateWhileTapped() {
        Permanent tome = harness.addToBattlefieldAndReturn(player1, new JalumTome());
        tome.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An activated ability still draws and discards after the Tome leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent tome = harness.addToBattlefieldAndReturn(player1, new JalumTome());
        Forest oldCard = new Forest();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(oldCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(tome);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(oldCard, drawnCard);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(oldCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating the ability taps Jalum Tome and puts the ability on the stack")
    void activatingTapsAndGoesOnStack() {
        Permanent tome = harness.addToBattlefieldAndReturn(player1, new JalumTome());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(tome.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability draws a card, then discards a card (net hand size unchanged)")
    void resolvingDrawsThenDiscards() {
        harness.addToBattlefield(player1, new JalumTome());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Drew a card, now awaiting the discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("The discard choice can discard the pre-existing card after the draw")
    void drawsBeforeDiscardChoice() {
        harness.addToBattlefield(player1, new JalumTome());
        Forest discardedCard = new Forest();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discardedCard, drawnCard);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
    }

    @Test
    @DisplayName("The drawn card can be discarded when the hand starts empty")
    void discardsDrawnCardWhenHandStartsEmpty() {
        harness.addToBattlefield(player1, new JalumTome());
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("The draw provides a card to discard when the hand starts empty")
    void drawsBeforeDiscardingWithEmptyHand() {
        harness.addToBattlefield(player1, new JalumTome());
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
    }
}
