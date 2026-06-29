package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartlessSummoningTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Heartless Summoning has correct effects")
    void hasCorrectEffects() {
        HeartlessSummoning card = new HeartlessSummoning();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(ReduceOwnCastCostForCardTypeEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(StaticBoostEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HeartlessSummoning()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Heartless Summoning");
    }

    @Test
    @DisplayName("Resolving puts Heartless Summoning onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new HeartlessSummoning()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Heartless Summoning"));
    }

    // ===== Static effect: -1/-1 to own creatures =====

    @Test
    @DisplayName("Own creatures get -1/-1")
    void debuffsOwnCreatures() {
        harness.addToBattlefield(player1, new HeartlessSummoning());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Grizzly Bears is 2/2, with -1/-1 should be 1/1
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's creatures do not get -1/-1")
    void doesNotDebuffOpponentCreatures() {
        harness.addToBattlefield(player1, new HeartlessSummoning());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Opponent's Grizzly Bears should remain 2/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature with 1 toughness dies from -1/-1 state-based action")
    void creatureWithOneToughnessDies() {
        harness.addToBattlefield(player1, new HeartlessSummoning());
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.runStateBasedActions();

        // EliteVanguard is 2/1, with -1/-1 becomes 1/0 and should die to SBA
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elite Vanguard"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));
    }

    // ===== Cost reduction: creature spells cost {2} less =====

    @Test
    @DisplayName("Creature spells cost {2} less to cast")
    void creatureSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new HeartlessSummoning());
        // Hill Giant costs {3}{R} — with {2} reduction it should cost {1}{R}
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hill Giant");
    }

    @Test
    @DisplayName("Cannot cast creature without enough mana even with cost reduction")
    void cannotCastCreatureWithoutEnoughMana() {
        harness.addToBattlefield(player1, new HeartlessSummoning());
        // Hill Giant costs {3}{R} — with {2} reduction it needs {1}{R}; just {R} is not enough
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-creature spells are not affected by cost reduction")
    void nonCreatureSpellsNotReduced() {
        harness.addToBattlefield(player1, new HeartlessSummoning());
        // Heartless Summoning costs {1}{B} — another copy should still cost {1}{B}, not reduced
        harness.setHand(player1, List.of(new HeartlessSummoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Only 1 black mana — not enough for {1}{B} since enchantments are not affected
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Multiple Heartless Summonings stack =====

    @Test
    @DisplayName("Two Heartless Summonings give -2/-2 to own creatures")
    void twoHeartlessSummoningsStackDebuff() {
        harness.addToBattlefield(player1, new HeartlessSummoning());
        harness.addToBattlefield(player1, new HeartlessSummoning());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.runStateBasedActions();

        // 2/2 base - 2/2 from two Heartless = 0/0 -> should die to SBA
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Two Heartless Summonings reduce creature cost by {4}")
    void twoHeartlessSummoningsStackReduction() {
        harness.addToBattlefield(player1, new HeartlessSummoning());
        harness.addToBattlefield(player1, new HeartlessSummoning());
        // Hill Giant costs {3}{R} — with {4} reduction it should cost just {R}
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hill Giant");
    }

    // ===== Debuff removed when source leaves =====

    @Test
    @DisplayName("Debuff is removed when Heartless Summoning leaves the battlefield")
    void debuffRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new HeartlessSummoning());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Heartless Summoning"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
