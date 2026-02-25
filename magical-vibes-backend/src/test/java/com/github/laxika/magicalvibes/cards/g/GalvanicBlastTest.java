package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftReplacementEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GalvanicBlastTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has MetalcraftReplacementEffect wrapping 2-damage base and 4-damage metalcraft")
    void hasCorrectStructure() {
        GalvanicBlast card = new GalvanicBlast();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(MetalcraftReplacementEffect.class);

        MetalcraftReplacementEffect effect =
                (MetalcraftReplacementEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.baseEffect()).isInstanceOf(DealDamageToAnyTargetEffect.class);
        assertThat(effect.metalcraftEffect()).isInstanceOf(DealDamageToAnyTargetEffect.class);
        assertThat(((DealDamageToAnyTargetEffect) effect.baseEffect()).damage()).isEqualTo(2);
        assertThat(((DealDamageToAnyTargetEffect) effect.metalcraftEffect()).damage()).isEqualTo(4);
    }

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Deals 2 damage to target player without metalcraft")
    void deals2DamageToPlayerWithoutMetalcraft() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature without metalcraft")
    void deals2DamageToCreatureWithoutMetalcraft() {
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // 2 damage kills a 2/2
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Deals 4 damage to target player with metalcraft")
    void deals4DamageToPlayerWithMetalcraft() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        addThreeArtifacts(player1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals 4 damage to target creature with metalcraft")
    void deals4DamageToCreatureWithMetalcraft() {
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        addThreeArtifacts(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Metalcraft lost before resolution =====

    @Test
    @DisplayName("Deals only 2 damage if metalcraft lost before resolution")
    void deals2DamageIfMetalcraftLostBeforeResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        addThreeArtifacts(player1);

        harness.castInstant(player1, 0, player2.getId());

        // Remove artifacts before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Should deal only 2 (base) since metalcraft is no longer met
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Helpers =====

    private void addThreeArtifacts(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new Spellbook());
        harness.addToBattlefield(player, new LeoninScimitar());
        harness.addToBattlefield(player, new Spellbook());
    }
}
