package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkaabRuinatorTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ExileNCardsFromGraveyardCost(3, CREATURE) as additional cost")
    void hasExileThreeCreaturesCost() {
        SkaabRuinator card = new SkaabRuinator();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ExileNCardsFromGraveyardCost.class);
        ExileNCardsFromGraveyardCost cost = (ExileNCardsFromGraveyardCost) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(cost.count()).isEqualTo(3);
        assertThat(cost.requiredType()).isEqualTo(CardType.CREATURE);
    }

    @Test
    @DisplayName("Has GraveyardCast casting option")
    void hasGraveyardCastOption() {
        SkaabRuinator card = new SkaabRuinator();

        assertThat(card.getCastingOption(GraveyardCast.class)).isPresent();
    }

    // ===== Casting from hand =====

    @Test
    @DisplayName("Can cast from hand by exiling 3 creature cards from graveyard")
    void castFromHandExilesThreeCreatures() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        GrizzlyBears bears3 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2, bears3));

        harness.setHand(player1, List.of(new SkaabRuinator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");

        // All 3 creature cards exiled
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerExiledCards.getOrDefault(player1.getId(), List.of())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot cast from hand with fewer than 3 creature cards in graveyard")
    void cannotCastWithFewerThanThreeCreatures() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));

        harness.setHand(player1, List.of(new SkaabRuinator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile non-creature cards to pay the additional cost")
    void cannotExileNonCreatureCards() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        GrizzlyBears bears3 = new GrizzlyBears();
        Shock shock = new Shock();
        // Need 3 creatures for playability, but we'll try to exile the Shock instead of bears3
        harness.setGraveyard(player1, List.of(bears1, bears2, bears3, shock));

        harness.setHand(player1, List.of(new SkaabRuinator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Try to exile indices 0, 1, 3 — index 3 is a Shock (instant), not a creature
        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot use duplicate graveyard indices")
    void cannotUseDuplicateIndices() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        GrizzlyBears bears3 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2, bears3));

        harness.setHand(player1, List.of(new SkaabRuinator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    @DisplayName("Resolves as a creature on the battlefield")
    void resolvesOnBattlefield() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        GrizzlyBears bears3 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2, bears3));

        harness.setHand(player1, List.of(new SkaabRuinator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skaab Ruinator");
    }

    // ===== Casting from graveyard =====

    @Test
    @DisplayName("Can cast from graveyard by exiling 3 other creature cards")
    void castFromGraveyardExilesThreeCreatures() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        GrizzlyBears bears3 = new GrizzlyBears();
        SkaabRuinator ruinator = new SkaabRuinator();
        harness.setGraveyard(player1, List.of(ruinator, bears1, bears2, bears3));

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Cast from graveyard index 0 (ruinator).
        // After removing ruinator from graveyard, bears are at indices 0, 1, 2.
        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");

        // All 3 bears exiled, ruinator removed from graveyard (on stack)
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast from graveyard without 3 other creature cards")
    void cannotCastFromGraveyardWithoutEnoughCreatures() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        SkaabRuinator ruinator = new SkaabRuinator();
        harness.setGraveyard(player1, List.of(ruinator, bears1, bears2));

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting from graveyard does not exile the card after resolution (unlike flashback)")
    void castFromGraveyardDoesNotExile() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        GrizzlyBears bears3 = new GrizzlyBears();
        SkaabRuinator ruinator = new SkaabRuinator();
        harness.setGraveyard(player1, List.of(ruinator, bears1, bears2, bears3));

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        // Should be on the battlefield, not exiled
        harness.assertOnBattlefield(player1, "Skaab Ruinator");
        assertThat(gd.playerExiledCards.getOrDefault(player1.getId(), List.of()))
                .noneMatch(c -> c.getName().equals("Skaab Ruinator"));
    }

    @Test
    @DisplayName("Casting from graveyard requires sorcery-speed timing")
    void castFromGraveyardRequiresSorceryTiming() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        GrizzlyBears bears3 = new GrizzlyBears();
        SkaabRuinator ruinator = new SkaabRuinator();
        harness.setGraveyard(player1, List.of(ruinator, bears1, bears2, bears3));

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Force to a non-main phase
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }
}
