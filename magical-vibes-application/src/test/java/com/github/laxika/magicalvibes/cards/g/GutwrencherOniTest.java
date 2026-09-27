package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BloodOgre;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GutwrencherOni.class, BloodOgre.class})
class GutwrencherOniTest extends BaseCardTest {

    // "At the beginning of your upkeep, discard a card if you don't control an Ogre."

    @Test
    @DisplayName("Without an Ogre, controller discards a chosen card")
    void discardsWithoutOgre() {
        harness.addToBattlefield(player1, new GutwrencherOni());
        harness.setHand(player1, List.of(new BloodOgre()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Blood Ogre");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Controlling an Ogre skips the discard")
    void noDiscardWithOgre() {
        harness.addToBattlefield(player1, new GutwrencherOni());
        harness.addToBattlefield(player1, new BloodOgre());
        harness.setHand(player1, List.of(new BloodOgre()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's Ogre does not stop the discard")
    void opponentOgreDoesNotHelp() {
        harness.addToBattlefield(player1, new GutwrencherOni());
        harness.addToBattlefield(player2, new BloodOgre());
        harness.setHand(player1, List.of(new BloodOgre()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @DisplayName("Triggers with an Ogre, then discards if the Ogre is gone when it resolves")
    void checksOgreAtResolutionAfterTrigger() {
        harness.addToBattlefield(player1, new GutwrencherOni());
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BloodOgre());
        harness.setHand(player1, List.of(new BloodOgre()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ogre));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Blood Ogre");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new GutwrencherOni());
        harness.setHand(player1, List.of(new BloodOgre()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
