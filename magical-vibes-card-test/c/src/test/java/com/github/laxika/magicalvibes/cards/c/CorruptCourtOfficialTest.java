package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorruptCourtOfficial.class})
class CorruptCourtOfficialTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack with selected opponent target")
    void resolvingPutsEtbOnStackWithTarget() {
        castCorruptCourtOfficial(player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Corrupt Court Official");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger makes target opponent discard one card")
    void etbMakesTargetOpponentDiscard() {
        harness.setHand(player2, List.of(new CorruptCourtOfficial()));
        castCorruptCourtOfficial(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Corrupt Court Official");
    }

    @Test
    @DisplayName("ETB trigger does nothing when target opponent has no cards in hand")
    void etbDoesNothingWithEmptyOpponentHand() {
        harness.setHand(player2, List.of());
        castCorruptCourtOfficial(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to discard"));
    }

    @Test
    @DisplayName("Cannot cast by targeting yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new CorruptCourtOfficial()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Target opponent chooses exactly one card from a larger hand")
    void opponentChoosesOneCardFromLargerHand() {
        CorruptCourtOfficial retained = new CorruptCourtOfficial();
        CorruptCourtOfficial discarded = new CorruptCourtOfficial();
        harness.setHand(player2, List.of(retained, discarded));
        castCorruptCourtOfficial(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent is determined relative to the creature's controller")
    void otherControllerMakesPlayerOneDiscard() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new CorruptCourtOfficial()));
        harness.setHand(player2, List.of(new CorruptCourtOfficial()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.ensurePriority(player2);
        harness.castCreature(player2, 0, player1.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player2, "Corrupt Court Official");
        harness.assertInGraveyard(player1, "Corrupt Court Official");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    private void castCorruptCourtOfficial(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new CorruptCourtOfficial()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, targetPlayerId);
    }
}
