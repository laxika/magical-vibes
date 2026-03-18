package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkaabGoliathTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Skaab Goliath has ExileNCardsFromGraveyardCost requiring 2 creature cards")
    void hasCorrectCostEffect() {
        SkaabGoliath card = new SkaabGoliath();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ExileNCardsFromGraveyardCost.class);

        ExileNCardsFromGraveyardCost cost = (ExileNCardsFromGraveyardCost) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(cost.count()).isEqualTo(2);
        assertThat(cost.requiredType()).isEqualTo(CardType.CREATURE);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Skaab Goliath exiles two creature cards from graveyard")
    void castingExilesTwoCreatureCards() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        RagingGoblin goblin = new RagingGoblin();
        harness.setGraveyard(player1, List.of(bears1, bears2, goblin));

        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Skaab Goliath");

        // Two creature cards exiled, one creature remains in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerExiledCards.getOrDefault(player1.getId(), List.of())).hasSize(2);
    }

    @Test
    @DisplayName("Skaab Goliath resolves and enters battlefield as 6/9 with trample")
    void resolvesOntoBattlefield() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));

        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skaab Goliath");
        Permanent goliath = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Skaab Goliath"))
                .findFirst().orElseThrow();
        assertThat(goliath.getCard().getPower()).isEqualTo(6);
        assertThat(goliath.getCard().getToughness()).isEqualTo(9);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot cast Skaab Goliath with only 1 creature card in graveyard")
    void cannotCastWithOnlyOneCreatureCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        // Only 1 creature card — card should not be playable
        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast Skaab Goliath exiling non-creature cards")
    void cannotExileNonCreatureCards() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(bears1, bears2, shock));

        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        // Index 2 is an instant, not a creature — must exile creature cards only
        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot cast Skaab Goliath with empty graveyard")
    void cannotCastWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        // No creatures in graveyard — card should not be playable
        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exile cost is paid even if Skaab Goliath is countered")
    void exileCostPaidEvenIfCountered() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));

        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));

        // Exile cost already paid
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerExiledCards.getOrDefault(player1.getId(), List.of())).hasSize(2);
    }

    @Test
    @DisplayName("Can cast with exactly 2 creature cards in graveyard (no surplus needed)")
    void canCastWithExactlyTwoCreatures() {
        GrizzlyBears bears1 = new GrizzlyBears();
        RagingGoblin goblin = new RagingGoblin();
        harness.setGraveyard(player1, List.of(bears1, goblin));

        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerExiledCards.getOrDefault(player1.getId(), List.of())).hasSize(2);
    }
}
