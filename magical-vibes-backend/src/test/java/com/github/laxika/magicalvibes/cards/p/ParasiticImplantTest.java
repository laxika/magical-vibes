package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureAndCreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParasiticImplantTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Parasitic Implant is an aura with upkeep trigger")
    void hasCorrectEffects() {
        ParasiticImplant card = new ParasiticImplant();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(SacrificeEnchantedCreatureAndCreateTokenEffect.class);
    }

    // ===== Upkeep trigger — sacrifice and token =====

    @Test
    @DisplayName("At controller's upkeep, enchanted creature is sacrificed and a Myr token is created")
    void upkeepSacrificesCreatureAndCreatesToken() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new ParasiticImplant()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Confirm creature is enchanted
        Permanent auraPerm = findPermanentByName(player1, "Parasitic Implant");
        assertThat(auraPerm.getAttachedTo()).isEqualTo(creature.getId());

        // Advance to player1's upkeep (aura controller)
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Enchanted creature should be gone
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getId().equals(creature.getId()))).isFalse();

        // Creature should be in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Aura should also be gone (orphaned)
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getCard().getName().equals("Parasitic Implant"))).isTrue();

        // A 1/1 Phyrexian Myr artifact creature token should exist for player1
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Myr")
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().hasType(CardType.ARTIFACT)
                        && p.getCard().isToken())).isTrue();
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new ParasiticImplant()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Advance to player2's upkeep (not the aura controller)
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        // Creature should still be alive
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getId().equals(creature.getId()))).isTrue();

        // No Myr token should exist
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getCard().getName().equals("Phyrexian Myr"))).isTrue();
    }

    @Test
    @DisplayName("Enchanting own creature — sacrifice and token work for same player")
    void enchantingOwnCreature() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new ParasiticImplant()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Own creature should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getId().equals(creature.getId()))).isTrue();

        // Should have a Myr token
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Myr"))).isTrue();
    }

    // ===== Helper methods =====

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanentByName(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
