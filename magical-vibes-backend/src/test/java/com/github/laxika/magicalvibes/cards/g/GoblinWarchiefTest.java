package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinWarchiefTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Goblin Warchief has cost reduction and haste-granting static effects")
    void hasCorrectEffects() {
        GoblinWarchief card = new GoblinWarchief();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(ReduceOwnCastCostForSubtypeEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(StaticBoostEffect.class);

        ReduceOwnCastCostForSubtypeEffect costEffect = (ReduceOwnCastCostForSubtypeEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(costEffect.affectedSubtypes()).containsExactly(CardSubtype.GOBLIN);
        assertThat(costEffect.amount()).isEqualTo(1);

        StaticBoostEffect hasteEffect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(hasteEffect.powerBoost()).isEqualTo(0);
        assertThat(hasteEffect.toughnessBoost()).isEqualTo(0);
        assertThat(hasteEffect.grantedKeywords()).containsExactly(Keyword.HASTE);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Goblin Warchief puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new GoblinWarchief()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Goblin Warchief");
    }

    @Test
    @DisplayName("Resolving puts Goblin Warchief onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new GoblinWarchief()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Warchief"));
    }

    // ===== Haste grant =====

    @Test
    @DisplayName("Other own Goblin creatures have haste")
    void grantsHasteToOwnGoblins() {
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        harness.addToBattlefield(player1, new GoblinWarchief());

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
        // No power/toughness boost
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not grant haste to non-Goblin creatures")
    void doesNotGrantHasteToNonGoblins() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinWarchief());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant haste to opponent's Goblins")
    void doesNotGrantHasteToOpponentGoblins() {
        harness.addToBattlefield(player1, new GoblinWarchief());
        harness.addToBattlefield(player2, new GoblinEliteInfantry());

        Permanent opponentGoblin = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, opponentGoblin, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste is removed when Goblin Warchief leaves the battlefield")
    void hasteRemovedWhenWarchiefLeaves() {
        harness.addToBattlefield(player1, new GoblinWarchief());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Goblin Warchief"));

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isFalse();
    }

    // ===== Cost reduction =====

    @Test
    @DisplayName("Goblin creature spells cost {1} less to cast")
    void goblinSpellsCostOneLess() {
        harness.addToBattlefield(player1, new GoblinWarchief());
        // Goblin Elite Infantry costs {1}{R} — with {1} reduction it should cost just {R}
        harness.setHand(player1, List.of(new GoblinEliteInfantry()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Elite Infantry");
    }

    @Test
    @DisplayName("Cannot cast Goblin spell without enough mana even with cost reduction")
    void cannotCastGoblinWithoutEnoughMana() {
        harness.addToBattlefield(player1, new GoblinWarchief());
        // Goblin Elite Infantry costs {1}{R} — with {1} reduction needs {R}; no mana is not enough
        harness.setHand(player1, List.of(new GoblinEliteInfantry()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Goblin creature spells are not reduced")
    void nonGoblinSpellsNotReduced() {
        harness.addToBattlefield(player1, new GoblinWarchief());
        // Grizzly Bears costs {1}{G} — should not be reduced
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Only {G} is not enough for {1}{G}
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cost reduction does not apply to opponent's Goblin spells")
    void doesNotReduceOpponentGoblinCosts() {
        harness.addToBattlefield(player1, new GoblinWarchief());
        // Opponent's Goblin Elite Infantry should still cost {1}{R}
        harness.setHand(player2, List.of(new GoblinEliteInfantry()));
        harness.addMana(player2, ManaColor.RED, 1);

        // Only {R} is not enough for {1}{R} — reduction does not apply to opponent
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Goblin Warchiefs reduce Goblin spell cost by {2}")
    void twoWarchiefsStackReduction() {
        harness.addToBattlefield(player1, new GoblinWarchief());
        harness.addToBattlefield(player1, new GoblinWarchief());
        // Goblin Warchief itself costs {1}{R}{R} — with {2} reduction the {1} generic is fully reduced,
        // cost is {R}{R} (generic cost cannot go below 0)
        harness.setHand(player1, List.of(new GoblinWarchief()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Warchief");
    }

    // ===== Both abilities together =====

    @Test
    @DisplayName("Goblin cast with cost reduction enters with haste from Warchief")
    void castGoblinWithCostReductionGetsHaste() {
        harness.addToBattlefield(player1, new GoblinWarchief());
        harness.setHand(player1, List.of(new GoblinEliteInfantry()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }
}
