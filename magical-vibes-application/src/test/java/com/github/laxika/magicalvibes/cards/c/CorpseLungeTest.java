package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorpseLungeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Corpse Lunge has correct effects")
    void hasCorrectEffects() {
        CorpseLunge card = new CorpseLunge();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ExileCardFromGraveyardCost.class);
        ExileCardFromGraveyardCost exileCost = (ExileCardFromGraveyardCost) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(exileCost.requiredType()).isEqualTo(CardType.CREATURE);
        assertThat(exileCost.trackExiledPower()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DealXDamageToTargetCreatureEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Corpse Lunge exiles a creature from graveyard and stores its power as X")
    void castingExilesCreatureAndStoresPower() {
        GrizzlyBears bears = new GrizzlyBears(); // 2/2
        harness.setGraveyard(player1, List.of(bears));

        Permanent target = new Permanent(new RagingGoblin());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new CorpseLunge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Corpse Lunge");
        assertThat(entry.getXValue()).isEqualTo(2); // Grizzly Bears has 2 power

        // Creature card should be exiled from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot cast Corpse Lunge without a creature in graveyard")
    void cannotCastWithoutCreatureInGraveyard() {
        Permanent target = new Permanent(new RagingGoblin());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new CorpseLunge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a non-creature card from graveyard")
    void cannotExileNonCreatureCard() {
        Shock shock = new Shock(); // Instant, not a creature
        harness.setGraveyard(player1, List.of(shock));

        Permanent target = new Permanent(new RagingGoblin());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new CorpseLunge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Corpse Lunge deals damage equal to exiled creature's power to target creature")
    void dealsDamageToCreature() {
        GrizzlyBears bears = new GrizzlyBears(); // 2/2
        harness.setGraveyard(player1, List.of(bears));

        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new CorpseLunge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 0);
        harness.passBothPriorities();

        // Target creature should be dead (2 damage to 2 toughness)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Corpse Lunge with 1-power creature deals 1 damage")
    void onePowerCreatureDealsOneDamage() {
        RagingGoblin goblin = new RagingGoblin(); // 1/1
        harness.setGraveyard(player1, List.of(goblin));

        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new CorpseLunge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 0);
        harness.passBothPriorities();

        // Target creature took 1 damage but survives (2 toughness)
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exile cost is paid even if spell fizzles due to target removal")
    void exileCostPaidEvenIfSpellFizzles() {
        GrizzlyBears bears = new GrizzlyBears(); // 2/2
        harness.setGraveyard(player1, List.of(bears));

        Permanent target = new Permanent(new RagingGoblin());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new CorpseLunge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 0);

        // Exile cost already paid
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(target.getId()));

        harness.passBothPriorities();

        // Creature was still exiled (cost is not refunded)
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can exile second creature from graveyard when multiple are present")
    void exilesCorrectCreatureByIndex() {
        RagingGoblin goblin = new RagingGoblin(); // 1/1
        GrizzlyBears bears = new GrizzlyBears(); // 2/2
        harness.setGraveyard(player1, List.of(goblin, bears));

        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new CorpseLunge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Exile the Grizzly Bears (index 1) to get 2 power
        harness.castInstantWithGraveyardExile(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        // 2 damage kills a 2/2
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Grizzly Bears exiled, Raging Goblin still in graveyard, Corpse Lunge went to graveyard after resolution
        harness.assertInGraveyard(player1, "Raging Goblin");
        harness.assertInGraveyard(player1, "Corpse Lunge");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
