package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RedSunsZenithTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Red Sun's Zenith has correct effects")
    void hasCorrectEffects() {
        RedSunsZenith card = new RedSunsZenith();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DealXDamageToAnyTargetEffect.class);
        assertThat(((DealXDamageToAnyTargetEffect) card.getEffects(EffectSlot.SPELL).get(0)).exileInsteadOfDie()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ShuffleIntoLibraryEffect.class);
    }

    // ===== Dealing damage to player =====

    @Test
    @DisplayName("Deals X damage to target player")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 5, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void xZeroDealsNoDamage() {
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Dealing damage to creature =====

    @Test
    @DisplayName("Deals X damage to target creature, destroying it")
    void dealsXDamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, 2, targetId);
        harness.passBothPriorities();

        // Grizzly Bears (2/2) should be destroyed by 2 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not destroy creature with toughness greater than X")
    void doesNotDestroyCreatureWithHigherToughness() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castSorcery(player1, 0, 3, targetId);
        harness.passBothPriorities();

        // Serra Angel (4/4) should survive 3 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Serra Angel"));
    }

    // ===== Exile instead of die =====

    @Test
    @DisplayName("Creature killed by Red Sun's Zenith is exiled instead of going to graveyard")
    void creatureKilledIsExiledInsteadOfDying() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, 2, targetId);
        harness.passBothPriorities();

        // Grizzly Bears should be exiled, NOT in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature that survives damage is not exiled")
    void creatureThatSurvivesIsNotExiled() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castSorcery(player1, 0, 1, targetId);
        harness.passBothPriorities();

        // Serra Angel should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Serra Angel"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Serra Angel"));
    }

    // ===== Shuffle into library =====

    @Test
    @DisplayName("Red Sun's Zenith is shuffled into owner's library instead of going to graveyard")
    void shuffledIntoLibraryAfterResolution() {
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // Should NOT be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Red Sun's Zenith"));
        // Should be shuffled into library
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Red Sun's Zenith"));
    }

    // ===== Stack behavior =====

    @Test
    @DisplayName("Casting puts it on the stack as sorcery")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Red Sun's Zenith");
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
