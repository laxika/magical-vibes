package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AscendantEvincarTest extends BaseCardTest {


    // ===== Card properties =====

    @Test
    @DisplayName("Ascendant Evincar has correct card properties")
    void hasCorrectProperties() {
        AscendantEvincar card = new AscendantEvincar();

        assertThat(card.getSupertypes()).containsExactly(CardSupertype.LEGENDARY);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(StaticBoostEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(StaticBoostEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new AscendantEvincar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Ascendant Evincar");
    }

    @Test
    @DisplayName("Resolving puts Ascendant Evincar onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new AscendantEvincar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ascendant Evincar"));
    }

    @Test
    @DisplayName("Enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new AscendantEvincar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ascendant Evincar"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Static effect: does not buff itself =====

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new AscendantEvincar());

        Permanent evincar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ascendant Evincar"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, evincar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, evincar)).isEqualTo(3);
    }

    // ===== Static effect: buffs other black creatures =====

    @Test
    @DisplayName("Own black creatures get +1/+1")
    void buffsOwnBlackCreatures() {
        harness.addToBattlefield(player1, new AscendantEvincar());
        harness.addToBattlefield(player1, new DrudgeSkeletons());

        Permanent skeletons = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drudge Skeletons"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's black creatures also get +1/+1")
    void buffsOpponentBlackCreatures() {
        harness.addToBattlefield(player1, new AscendantEvincar());
        harness.addToBattlefield(player2, new DrudgeSkeletons());

        Permanent opponentSkeletons = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drudge Skeletons"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, opponentSkeletons)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSkeletons)).isEqualTo(2);
    }

    // ===== Static effect: debuffs nonblack creatures =====

    @Test
    @DisplayName("Own nonblack creatures get -1/-1")
    void debuffsOwnNonblackCreatures() {
        harness.addToBattlefield(player1, new AscendantEvincar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's nonblack creatures get -1/-1")
    void debuffsOpponentNonblackCreatures() {
        harness.addToBattlefield(player1, new AscendantEvincar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(1);
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Ascendant Evincars buff each other")
    void twoEvincarsBuffEachOther() {
        harness.addToBattlefield(player1, new AscendantEvincar());
        harness.addToBattlefield(player1, new AscendantEvincar());

        List<Permanent> evincars = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ascendant Evincar"))
                .toList();

        assertThat(evincars).hasSize(2);
        for (Permanent evincar : evincars) {
            // Each gets +1/+1 from the other → 4/4
            assertThat(gqs.getEffectivePower(gd, evincar)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, evincar)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("Two Ascendant Evincars give +2/+2 to other black creatures")
    void twoEvincarsStackBlackBonus() {
        harness.addToBattlefield(player1, new AscendantEvincar());
        harness.addToBattlefield(player1, new AscendantEvincar());
        harness.addToBattlefield(player1, new DrudgeSkeletons());

        Permanent skeletons = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drudge Skeletons"))
                .findFirst().orElseThrow();

        // 1/1 base + 2/2 from two Evincars = 3/3
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two Ascendant Evincars give -2/-2 to nonblack creatures")
    void twoEvincarsStackNonblackPenalty() {
        harness.addToBattlefield(player1, new AscendantEvincar());
        harness.addToBattlefield(player1, new AscendantEvincar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // 2/2 base - 2/2 from two Evincars = 0/0
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(0);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Black creature bonus is removed when Ascendant Evincar leaves")
    void blackBonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new AscendantEvincar());
        harness.addToBattlefield(player1, new DrudgeSkeletons());

        Permanent skeletons = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drudge Skeletons"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Ascendant Evincar"));

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(1);
    }

    @Test
    @DisplayName("Nonblack creature penalty is removed when Ascendant Evincar leaves")
    void nonblackPenaltyRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new AscendantEvincar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Ascendant Evincar"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Bonus applies on resolve =====

    @Test
    @DisplayName("Bonus applies when Ascendant Evincar resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AscendantEvincar()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        Permanent skeletons = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drudge Skeletons"))
                .findFirst().orElseThrow();
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Before casting, no bonus
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // After resolving, black creature buffed, nonblack debuffed
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new AscendantEvincar());
        harness.addToBattlefield(player1, new DrudgeSkeletons());

        Permanent skeletons = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drudge Skeletons"))
                .findFirst().orElseThrow();

        // Simulate a temporary spell boost
        skeletons.setPowerModifier(skeletons.getPowerModifier() + 3);
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(5); // 1 base + 3 spell + 1 static

        // Reset end-of-turn modifiers
        skeletons.resetModifiers();

        // Spell bonus gone, static bonus still computed
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2); // 1 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(2);
    }

    // ===== Flying keyword =====

    @Test
    @DisplayName("Ascendant Evincar has flying")
    void hasFlying() {
        harness.addToBattlefield(player1, new AscendantEvincar());

        Permanent evincar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ascendant Evincar"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, evincar, Keyword.FLYING)).isTrue();
    }
}

