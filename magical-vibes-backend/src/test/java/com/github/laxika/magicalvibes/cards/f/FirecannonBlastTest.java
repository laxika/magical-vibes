package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RaidReplacementEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FirecannonBlastTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has RaidReplacementEffect wrapping 3-damage base and 6-damage raid")
    void hasCorrectStructure() {
        FirecannonBlast card = new FirecannonBlast();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(RaidReplacementEffect.class);

        RaidReplacementEffect effect =
                (RaidReplacementEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.baseEffect()).isInstanceOf(DealDamageToTargetCreatureEffect.class);
        assertThat(effect.raidEffect()).isInstanceOf(DealDamageToTargetCreatureEffect.class);
        assertThat(((DealDamageToTargetCreatureEffect) effect.baseEffect()).damage()).isEqualTo(3);
        assertThat(((DealDamageToTargetCreatureEffect) effect.raidEffect()).damage()).isEqualTo(6);
    }

    // ===== Without raid =====

    @Test
    @DisplayName("Deals 3 damage to target creature without raid")
    void deals3DamageWithoutRaid() {
        harness.setHand(player1, List.of(new FirecannonBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // 3 damage kills a 2/2
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Deals only 3 damage without raid — 4/4 creature survives")
    void fourToughnessCreatureSurvivesWithoutRaid() {
        harness.setHand(player1, List.of(new FirecannonBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new AirElemental());

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // 3 damage does not kill a 4/4
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
    }

    // ===== With raid =====

    @Test
    @DisplayName("Deals 6 damage with raid — kills a 4/4 creature")
    void deals6DamageWithRaid() {
        harness.setHand(player1, List.of(new FirecannonBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new AirElemental());

        // Simulate having attacked this turn
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // 6 damage kills a 4/4
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Air Elemental"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Air Elemental"));
    }

    // ===== Raid only checks controller =====

    @Test
    @DisplayName("Opponent attacking does not enable raid for caster")
    void opponentAttackingDoesNotEnableRaid() {
        harness.setHand(player1, List.of(new FirecannonBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new AirElemental());

        // Opponent attacked, not the caster
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // Only 3 damage — 4/4 survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
    }

    // ===== Raid checked at resolution time =====

    @Test
    @DisplayName("Raid is checked at resolution time, not cast time")
    void raidCheckedAtResolution() {
        harness.setHand(player1, List.of(new FirecannonBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new AirElemental());

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        // No raid when casting
        harness.castSorcery(player1, 0, targetId);

        // Raid becomes active after casting but before resolution
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.passBothPriorities();

        // Should deal 6 since raid is met at resolution time
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Air Elemental"));
    }
}
