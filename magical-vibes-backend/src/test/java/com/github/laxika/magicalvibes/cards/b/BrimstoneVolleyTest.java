package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MorbidReplacementEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BrimstoneVolleyTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has MorbidReplacementEffect wrapping 3-damage base and 5-damage morbid")
    void hasCorrectStructure() {
        BrimstoneVolley card = new BrimstoneVolley();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(MorbidReplacementEffect.class);

        MorbidReplacementEffect effect =
                (MorbidReplacementEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.baseEffect()).isInstanceOf(DealDamageToAnyTargetEffect.class);
        assertThat(effect.morbidEffect()).isInstanceOf(DealDamageToAnyTargetEffect.class);
        assertThat(((DealDamageToAnyTargetEffect) effect.baseEffect()).damage()).isEqualTo(3);
        assertThat(((DealDamageToAnyTargetEffect) effect.morbidEffect()).damage()).isEqualTo(5);
    }

    // ===== Without morbid =====

    @Test
    @DisplayName("Deals 3 damage to target player without morbid")
    void deals3DamageToPlayerWithoutMorbid() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 3 damage to target creature without morbid")
    void deals3DamageToCreatureWithoutMorbid() {
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // 3 damage kills a 2/2
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== With morbid =====

    @Test
    @DisplayName("Deals 5 damage to target player with morbid")
    void deals5DamageToPlayerWithMorbid() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        // Simulate a creature having died this turn
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Deals 5 damage to target creature with morbid")
    void deals5DamageToCreatureWithMorbid() {
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Simulate a creature having died this turn
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Morbid triggers from opponent's creature dying too")
    void morbidTriggersFromOpponentCreatureDying() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        // Opponent's creature died this turn
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should deal 5 (morbid) — doesn't matter whose creature died
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    // ===== Morbid lost before resolution =====

    @Test
    @DisplayName("Morbid is checked at resolution time, not cast time")
    void morbidCheckedAtResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        // No creature has died when casting
        harness.castInstant(player1, 0, player2.getId());

        // Creature dies after casting but before resolution
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.passBothPriorities();

        // Should deal 5 since morbid is met at resolution time
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    // ===== Integration: actual creature death enables morbid =====

    @Test
    @DisplayName("Killing a creature with Shock enables morbid for Brimstone Volley")
    void actualCreatureDeathEnablesMorbid() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Cast Shock targeting Grizzly Bears (2 damage kills a 2/2)
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        // Bears should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Now cast Brimstone Volley targeting player2 — morbid should be active
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should deal 5 damage (morbid), player2 started at 20
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
