package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenousRats.class, Forest.class})
class RavenousRatsTest extends BaseCardTest {

    

    

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack with selected opponent target")
    void resolvingPutsEtbOnStackWithTarget() {
        castRavenousRats(player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ravenous Rats");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger chooses an opponent after the creature enters")
    void etbTriggerChoosesTargetWhenPutOnStack() {
        castRavenousRats();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPermanentIds()).isEmpty();
        assertThat(targetChoice.validPlayerIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger makes target opponent discard one card")
    void etbMakesTargetOpponentDiscard() {
        harness.setHand(player2, List.of(new Forest()));
        castRavenousRats(player2.getId());

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("ETB trigger does nothing when target opponent has no cards in hand")
    void etbDoesNothingWithEmptyOpponentHand() {
        harness.setHand(player2, List.of());
        castRavenousRats(player2.getId());

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to discard"));
    }

    @Test
    @DisplayName("Cannot choose yourself as the ETB trigger's target")
    void cannotTargetYourself() {
        castRavenousRats();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    private void castRavenousRats() {
        harness.castFromHand(player1, new RavenousRats(), "{1}{B}");
    }

    private void castRavenousRats(UUID targetPlayerId) {
        prepareRavenousRats();
        harness.castCreature(player1, 0, targetPlayerId);
    }

    private void prepareRavenousRats() {
        harness.setHand(player1, List.of(new RavenousRats()));
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    @Test
    @DisplayName("ETB target is chosen after the creature enters when the spell was cast without one")
    void choosesTargetWhenEtbTriggerIsPutOnStack() {
        harness.castFromHand(player1, new RavenousRats(), "{1}{B}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent chooses exactly one card from a larger hand")
    void opponentChoosesWhichCardToDiscard() {
        Forest retained = new Forest();
        RavenousRats discarded = new RavenousRats();
        harness.setHand(player2, List.of(retained, discarded));
        castRavenousRats();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        resolveAllTriggers();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
