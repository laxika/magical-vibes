package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
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

class HeartlessPillageTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has discard effect and raid conditional treasure effect targeting opponent")
    void hasCorrectStructure() {
        HeartlessPillage card = new HeartlessPillage();

        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);

        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(TargetPlayerDiscardsEffect.class);
        TargetPlayerDiscardsEffect discard =
                (TargetPlayerDiscardsEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(discard.amount()).isEqualTo(2);

        assertThat(card.getEffects(EffectSlot.SPELL).get(1))
                .isInstanceOf(RaidConditionalEffect.class);
        RaidConditionalEffect raid =
                (RaidConditionalEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(raid.wrapped()).isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect treasure = (CreateTokenEffect) raid.wrapped();
        assertThat(treasure.primaryType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.amount()).isEqualTo(1);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting opponent")
    void castingPutsOnStack() {
        castHeartlessPillage();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Heartless Pillage");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new HeartlessPillage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    // ===== Discard without raid =====

    @Test
    @DisplayName("Opponent discards two cards without raid — no treasure created")
    void discardWithoutRaid() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        castHeartlessPillage();
        harness.passBothPriorities();

        // Opponent prompted to discard
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.revealedHandChoice().discardRemainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));

        // No treasure tokens without raid
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("No discard prompt when opponent has empty hand — no treasure without raid")
    void emptyHandNoRaid() {
        harness.setHand(player2, new ArrayList<>());
        castHeartlessPillage();
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to discard"));

        // No treasure tokens without raid
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList()).isEmpty();
    }

    // ===== Discard with raid =====

    @Test
    @DisplayName("Opponent discards two cards and a Treasure token is created with raid")
    void discardWithRaid() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        markAttackedThisTurn();
        castHeartlessPillage();
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        // One Treasure token created for the caster
        List<Permanent> treasures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList();
        assertThat(treasures).hasSize(1);
        Permanent treasure = treasures.getFirst();
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).containsExactly(CardSubtype.TREASURE);
        assertThat(treasure.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Treasure token created even when opponent has empty hand with raid")
    void emptyHandWithRaid() {
        harness.setHand(player2, new ArrayList<>());
        markAttackedThisTurn();
        castHeartlessPillage();
        harness.passBothPriorities();

        // No discard prompt
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to discard"));

        // Treasure token still created because raid is met
        List<Permanent> treasures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList();
        assertThat(treasures).hasSize(1);
    }

    // ===== Raid checked at resolution =====

    @Test
    @DisplayName("Raid is checked at resolution time, not cast time")
    void raidCheckedAtResolution() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        castHeartlessPillage();

        // Raid becomes active after casting but before resolution
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        // Treasure created because raid was met at resolution time
        List<Permanent> treasures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList();
        assertThat(treasures).hasSize(1);
    }

    @Test
    @DisplayName("Raid lost before resolution — no treasure created")
    void raidLostBeforeResolution() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        markAttackedThisTurn();
        castHeartlessPillage();

        // Remove the raid flag before resolution
        gd.playersDeclaredAttackersThisTurn.clear();

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        // Discard still happened
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        // No treasure because raid was lost
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList()).isEmpty();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("raid ability does nothing"));
    }

    // ===== Opponent attacking does not enable raid =====

    @Test
    @DisplayName("Opponent attacking does not enable raid for caster")
    void opponentAttackingDoesNotEnableRaid() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        castHeartlessPillage();
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        // No treasure — opponent attacked, not caster
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList()).isEmpty();
    }

    // ===== Spell goes to graveyard =====

    @Test
    @DisplayName("Goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        castHeartlessPillage();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Heartless Pillage"));
    }

    // ===== Helpers =====

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castHeartlessPillage() {
        harness.setHand(player1, List.of(new HeartlessPillage()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, player2.getId());
    }
}
