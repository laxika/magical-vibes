package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianasSpecter.class, RuneclawBear.class, Unsummon.class, LeylineOfSanctity.class})
class LilianasSpecterTest extends BaseCardTest {

    

    

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingPutsEtbOnStack() {
        castLilianasSpecter();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Liliana's Specter");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB trigger makes opponent discard one card")
    void etbMakesOpponentDiscard() {
        harness.setHand(player2, List.of(new RuneclawBear()));
        castLilianasSpecter();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("ETB trigger does nothing when opponent has no cards in hand")
    void etbDoesNothingWithEmptyOpponentHand() {
        harness.setHand(player2, List.of());
        castLilianasSpecter();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to discard"));
    }

    private void castLilianasSpecter() {
        harness.castFromHand(player1, new LilianasSpecter(), "{1}{B}{B}");
    }

    @Test
    @DisplayName("Opponent chooses exactly one card and controller keeps their hand")
    void opponentChoosesOneCard() {
        harness.setHand(player2, List.of(new RuneclawBear(), new Unsummon()));
        castLilianasSpecter();
        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Unsummon");
        harness.assertInHand(player2, "Runeclaw Bear");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player1, "Runeclaw Bear");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent with hexproof still discards because the ability does not target")
    void opponentHexproofDoesNotPreventDiscard() {
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setHand(player2, List.of(new RuneclawBear()));
        castLilianasSpecter();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Runeclaw Bear");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB trigger resolves after the Specter returns to hand")
    void triggerResolvesAfterSourceLeaves() {
        harness.setHand(player2, List.of(new RuneclawBear()));
        castLilianasSpecter();
        harness.passBothPriorities();
        var specterId = harness.getPermanentId(player1, "Liliana's Specter");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, specterId);
        harness.assertInHand(player1, "Liliana's Specter");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Runeclaw Bear");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
