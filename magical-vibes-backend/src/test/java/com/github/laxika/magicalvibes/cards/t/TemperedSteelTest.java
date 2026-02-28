package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TemperedSteelTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TemperedSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Tempered Steel");
    }

    @Test
    @DisplayName("Resolving puts Tempered Steel onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new TemperedSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Tempered Steel"));
    }

    // ===== Static effect: buffs own artifact creatures =====

    @Test
    @DisplayName("Own artifact creatures get +2/+2")
    void buffsOwnArtifactCreatures() {
        harness.addToBattlefield(player1, new TemperedSteel());
        harness.addToBattlefield(player1, new GoldMyr());

        Permanent myr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gold Myr"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(3);   // 1 base + 2
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(3); // 1 base + 2
    }

    @Test
    @DisplayName("Does not buff non-artifact creatures")
    void doesNotBuffNonArtifactCreatures() {
        harness.addToBattlefield(player1, new TemperedSteel());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-creature artifacts")
    void doesNotBuffNonCreatureArtifacts() {
        harness.addToBattlefield(player1, new TemperedSteel());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent spellbook = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spellbook"))
                .findFirst().orElseThrow();

        // Spellbook is a non-creature artifact, should not be affected
        assertThat(gqs.getEffectivePower(gd, spellbook)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, spellbook)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not buff opponent's artifact creatures")
    void doesNotBuffOpponentArtifactCreatures() {
        harness.addToBattlefield(player1, new TemperedSteel());
        harness.addToBattlefield(player2, new GoldMyr());

        Permanent opponentMyr = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gold Myr"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, opponentMyr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentMyr)).isEqualTo(1);
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Tempered Steels give +4/+4 to artifact creatures")
    void twoTemperedSteelsStack() {
        harness.addToBattlefield(player1, new TemperedSteel());
        harness.addToBattlefield(player1, new TemperedSteel());
        harness.addToBattlefield(player1, new GoldMyr());

        Permanent myr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gold Myr"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(5);   // 1 base + 4
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(5); // 1 base + 4
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Tempered Steel leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new TemperedSteel());
        harness.addToBattlefield(player1, new GoldMyr());

        Permanent myr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gold Myr"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Tempered Steel"));

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(1);
    }

    // ===== Bonus applies on resolve =====

    @Test
    @DisplayName("Bonus applies when Tempered Steel resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new GoldMyr());
        harness.setHand(player1, List.of(new TemperedSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent myr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gold Myr"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(3);
    }
}
