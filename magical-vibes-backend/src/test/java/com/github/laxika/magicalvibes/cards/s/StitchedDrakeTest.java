package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StitchedDrakeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Stitched Drake has exile creature from graveyard cost")
    void hasCorrectEffects() {
        StitchedDrake card = new StitchedDrake();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ExileCardFromGraveyardCost.class);
        ExileCardFromGraveyardCost exileCost = (ExileCardFromGraveyardCost) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(exileCost.requiredType()).isEqualTo(CardType.CREATURE);
        assertThat(exileCost.trackExiledPower()).isFalse();
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Stitched Drake exiles a creature card from graveyard")
    void castingExilesCreatureFromGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Stitched Drake");

        // Creature card should be exiled from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerExiledCards.getOrDefault(player1.getId(), List.of()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot cast Stitched Drake without a creature in graveyard")
    void cannotCastWithoutCreatureInGraveyard() {
        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a non-creature card from graveyard for Stitched Drake")
    void cannotExileNonCreatureCard() {
        Shock shock = new Shock(); // Instant, not a creature
        harness.setGraveyard(player1, List.of(shock));

        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Stitched Drake enters the battlefield as a 3/4 after resolving")
    void entersAsThreeFour() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stitched Drake");
        var drake = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Stitched Drake"))
                .findFirst().orElseThrow();
        assertThat(drake.getCard().getPower()).isEqualTo(3);
        assertThat(drake.getCard().getToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Exile cost is paid even if Stitched Drake is countered")
    void exileCostPaidEvenIfCountered() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);

        // Exile cost already paid
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerExiledCards.getOrDefault(player1.getId(), List.of()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can exile second creature from graveyard when multiple are present")
    void exilesCorrectCreatureByIndex() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock(); // Non-creature, should stay
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, shock, bears2));

        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Exile the second Grizzly Bears (index 2)
        harness.castCreatureWithGraveyardExile(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stitched Drake");
        // Shock and first Grizzly Bears remain in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }
}
