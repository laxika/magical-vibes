package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShriekingGrotesque.class, GrizzlyBears.class})
class ShriekingGrotesqueTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the target player discard when black mana was spent to cast it")
    void discardsWhenBlackManaWasSpent() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castShriekingGrotesque(player2.getId(), ManaColor.BLACK, 2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger when no black mana was spent to cast it")
    void doesNotTriggerWithoutBlackMana() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castShriekingGrotesque(player2.getId(), ManaColor.WHITE, 3);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetItsController() {
        harness.setHand(player1, new ArrayList<>(List.of(new ShriekingGrotesque(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, player1.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
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
