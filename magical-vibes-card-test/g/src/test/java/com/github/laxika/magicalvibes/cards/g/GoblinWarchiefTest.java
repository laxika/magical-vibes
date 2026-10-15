package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinWarchief.class, GoblinEliteInfantry.class, GrizzlyBears.class})
class GoblinWarchiefTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Goblin Warchief puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new GoblinWarchief(), "{1}{R}{R}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Goblin Warchief");
    }

    @Test
    @DisplayName("Resolving puts Goblin Warchief onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new GoblinWarchief(), "{1}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Goblin Warchief");
    }

    // ===== Haste grant =====

    @Test
    @DisplayName("Goblin Warchief itself has haste")
    void grantsHasteToItself() {
        Permanent warchief = harness.addToBattlefieldAndReturn(player1, new GoblinWarchief());

        assertThat(gqs.hasKeyword(gd, warchief, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Other own Goblin creatures have haste")
    void grantsHasteToOwnGoblins() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinEliteInfantry());
        harness.addToBattlefield(player1, new GoblinWarchief());

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
        // No power/toughness boost
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not grant haste to non-Goblin creatures")
    void doesNotGrantHasteToNonGoblins() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinWarchief());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant haste to opponent's Goblins")
    void doesNotGrantHasteToOpponentGoblins() {
        harness.addToBattlefield(player1, new GoblinWarchief());
        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinEliteInfantry());

        assertThat(gqs.hasKeyword(gd, opponentGoblin, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste is removed when Goblin Warchief leaves the battlefield")
    void hasteRemovedWhenWarchiefLeaves() {
        Permanent warchief = harness.addToBattlefieldAndReturn(player1, new GoblinWarchief());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinEliteInfantry());

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(warchief);

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isFalse();
    }

    // ===== Cost reduction =====

    @Test
    @DisplayName("Goblin creature spells cost {1} less to cast")
    void goblinSpellsCostOneLess() {
        harness.addToBattlefield(player1, new GoblinWarchief());
        // Goblin Elite Infantry costs {1}{R} — with {1} reduction it should cost just {R}
        harness.castFromHand(player1, new GoblinEliteInfantry(), "{R}");

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
        harness.castFromHand(player1, new GoblinWarchief(), "{R}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Warchief");
    }

    // ===== Both abilities together =====

    @Test
    @DisplayName("Goblin cast with cost reduction enters with haste from Warchief")
    void castGoblinWithCostReductionGetsHaste() {
        harness.addToBattlefield(player1, new GoblinWarchief());
        harness.castFromHand(player1, new GoblinEliteInfantry(), "{R}");
        harness.passBothPriorities();

        Permanent goblin = findPermanent(player1, "Goblin Elite Infantry");

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Multiple Warchiefs cannot reduce colored mana requirements")
    void reductionsDoNotRemoveColoredMana() {
        harness.addToBattlefield(player1, new GoblinWarchief());
        harness.addToBattlefield(player1, new GoblinWarchief());
        harness.setHand(player1, List.of(new GoblinWarchief()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cost reduction ends when the Warchief leaves")
    void costReductionEndsWhenWarchiefLeaves() {
        Permanent warchief = harness.addToBattlefieldAndReturn(player1, new GoblinWarchief());
        gd.playerBattlefields.get(player1.getId()).remove(warchief);
        harness.setHand(player1, List.of(new GoblinEliteInfantry()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A remaining Warchief continues granting haste after another leaves")
    void hasteRemainsWithAnotherWarchief() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoblinWarchief());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GoblinWarchief());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinEliteInfantry());

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A Warchief in hand does not reduce its own casting cost")
    void noCostReductionFromHand() {
        harness.setHand(player1, List.of(new GoblinWarchief()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
