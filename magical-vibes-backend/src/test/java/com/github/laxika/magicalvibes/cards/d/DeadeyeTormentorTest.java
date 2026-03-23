package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeadeyeTormentorTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has raid-conditional ETB discard effect targeting opponent")
    void hasRaidEtbDiscardEffect() {
        DeadeyeTormentor card = new DeadeyeTormentor();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(RaidConditionalEffect.class);

        RaidConditionalEffect raid =
                (RaidConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(raid.wrapped()).isInstanceOf(TargetPlayerDiscardsEffect.class);

        TargetPlayerDiscardsEffect discard = (TargetPlayerDiscardsEffect) raid.wrapped();
        assertThat(discard.amount()).isEqualTo(1);
    }

    // ===== ETB with raid met =====

    @Test
    @DisplayName("ETB triggers discard when raid is met (attacked this turn)")
    void etbTriggersWithRaid() {
        markAttackedThisTurn();
        castDeadeyeTormentor();
        harness.passBothPriorities(); // resolve creature spell

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Deadeye Tormentor");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB raid trigger makes target opponent discard a card")
    void etbMakesOpponentDiscardWithRaid() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        markAttackedThisTurn();
        castDeadeyeTormentor();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.revealedHandChoice().discardRemainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB raid trigger does nothing when opponent has empty hand")
    void etbDoesNothingWithEmptyOpponentHand() {
        harness.setHand(player2, new ArrayList<>());
        markAttackedThisTurn();
        castDeadeyeTormentor();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to discard"));
    }

    // ===== ETB without raid =====

    @Test
    @DisplayName("ETB does NOT trigger without raid (did not attack this turn)")
    void etbDoesNotTriggerWithoutRaid() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castDeadeyeTormentor();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();

        // Creature is still on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Deadeye Tormentor"));

        // Opponent hand unchanged — still has the card
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Raid lost before resolution (intervening-if) =====

    @Test
    @DisplayName("ETB does nothing if raid condition is lost before resolution")
    void etbFizzlesWhenRaidLost() {
        markAttackedThisTurn();
        castDeadeyeTormentor();
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack

        // Remove the raid flag before ETB resolves (simulating turn state cleared)
        gd.playersDeclaredAttackersThisTurn.clear();

        harness.passBothPriorities(); // resolve ETB trigger — raid no longer met

        // Life totals unchanged, opponent hand unchanged
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("raid ability does nothing"));
    }

    // ===== Creature enters battlefield regardless =====

    @Test
    @DisplayName("Creature enters battlefield even without raid")
    void creatureEntersWithoutRaid() {
        castDeadeyeTormentor();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Deadeye Tormentor"));
    }

    @Test
    @DisplayName("Stack is empty after full resolution with raid")
    void stackEmptyAfterResolution() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        markAttackedThisTurn();
        castDeadeyeTormentor();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
        harness.handleCardChosen(player2, 0); // opponent chooses a card to discard

        assertThat(gd.stack).isEmpty();
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot cast targeting yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new DeadeyeTormentor()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    // ===== Helpers =====

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castDeadeyeTormentor() {
        harness.setHand(player1, List.of(new DeadeyeTormentor()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);
    }
}
