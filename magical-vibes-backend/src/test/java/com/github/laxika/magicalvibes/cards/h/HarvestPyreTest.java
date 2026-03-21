package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HarvestPyreTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Harvest Pyre has correct effects")
    void hasCorrectEffects() {
        HarvestPyre card = new HarvestPyre();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ExileXCardsFromGraveyardCost.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DealXDamageToTargetCreatureEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Harvest Pyre exiles chosen cards from graveyard and sets X to count")
    void castingExilesCardsAndSetsX() {
        RagingGoblin goblin = new RagingGoblin();
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(goblin, bears, shock));

        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Exile 2 cards (indices 0 and 1)
        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Harvest Pyre");
        assertThat(entry.getXValue()).isEqualTo(2); // 2 cards exiled

        // Two cards should be exiled from graveyard, one remains
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can cast Harvest Pyre exiling zero cards (X=0)")
    void canCastWithZeroExiles() {
        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getXValue()).isEqualTo(0);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Harvest Pyre deals X damage equal to number of exiled cards")
    void dealsDamageEqualToExiledCount() {
        RagingGoblin goblin = new RagingGoblin();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(goblin, bears));

        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Exile both cards (X=2), enough to kill a 2/2
        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0, 1));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Harvest Pyre with 1 card exiled deals 1 damage")
    void oneCardExiledDealsOneDamage() {
        RagingGoblin goblin = new RagingGoblin();
        harness.setGraveyard(player1, List.of(goblin));

        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0));
        harness.passBothPriorities();

        // 1 damage doesn't kill a 2/2
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Harvest Pyre with X=0 deals no damage")
    void zeroExilesDealsNoDamage() {
        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        // 0 damage, creature survives
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can exile any card type from graveyard (not restricted to creatures)")
    void canExileAnyCardType() {
        Shock shock = new Shock(); // Instant
        harness.setGraveyard(player1, List.of(shock));

        Permanent target = new Permanent(new RagingGoblin()); // 1/1
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Exile the instant (X=1)
        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0));
        harness.passBothPriorities();

        // 1 damage kills a 1/1
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Exile cost is paid even if spell fizzles due to target removal")
    void exileCostPaidEvenIfSpellFizzles() {
        RagingGoblin goblin = new RagingGoblin();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(goblin, bears));

        Permanent target = new Permanent(new RagingGoblin());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0, 1));

        // Exile cost already paid
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(target.getId()));

        harness.passBothPriorities();

        // Cards are still exiled (cost is not refunded)
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }
}
