package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyrGalvanizerTest extends BaseCardTest {

    // ===== Static effect: Other Myr creatures you control get +1/+1 =====

    @Test
    @DisplayName("Other Myr creatures you control get +1/+1")
    void buffsOtherOwnMyr() {
        harness.addToBattlefield(player1, new MyrGalvanizer());
        harness.addToBattlefield(player1, new CopperMyr());

        Permanent copperMyr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Copper Myr"))
                .findFirst().orElseThrow();

        // Copper Myr is 1/1 base, should be 2/2 with Galvanizer
        assertThat(gqs.getEffectivePower(gd, copperMyr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copperMyr)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new MyrGalvanizer());

        Permanent galvanizer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr Galvanizer"))
                .findFirst().orElseThrow();

        // 2/2 base, no self-buff
        assertThat(gqs.getEffectivePower(gd, galvanizer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, galvanizer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Myr creatures")
    void doesNotBuffNonMyr() {
        harness.addToBattlefield(player1, new MyrGalvanizer());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Myr creatures")
    void doesNotBuffOpponentMyr() {
        harness.addToBattlefield(player1, new MyrGalvanizer());
        harness.addToBattlefield(player2, new CopperMyr());

        Permanent opponentMyr = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Copper Myr"))
                .findFirst().orElseThrow();

        // Should remain 1/1 — only buffs YOUR Myr
        assertThat(gqs.getEffectivePower(gd, opponentMyr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentMyr)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Galvanizers buff each other")
    void twoGalvanizersBuffEachOther() {
        harness.addToBattlefield(player1, new MyrGalvanizer());
        harness.addToBattlefield(player1, new MyrGalvanizer());

        List<Permanent> galvanizers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr Galvanizer"))
                .toList();

        assertThat(galvanizers).hasSize(2);
        for (Permanent g : galvanizers) {
            // 2/2 base + 1/1 from the other Galvanizer = 3/3
            assertThat(gqs.getEffectivePower(gd, g)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, g)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Bonus is removed when Myr Galvanizer leaves the battlefield")
    void bonusRemovedWhenGalvanizerLeaves() {
        harness.addToBattlefield(player1, new MyrGalvanizer());
        harness.addToBattlefield(player1, new CopperMyr());

        Permanent copperMyr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Copper Myr"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, copperMyr)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Myr Galvanizer"));

        assertThat(gqs.getEffectivePower(gd, copperMyr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, copperMyr)).isEqualTo(1);
    }

    // ===== Activated ability: {1}, {T}: Untap each other Myr you control =====

    @Test
    @DisplayName("Activating ability untaps other tapped Myr you control")
    void untapsOtherTappedMyr() {
        Permanent galvanizer = new Permanent(new MyrGalvanizer());
        galvanizer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(galvanizer);

        Permanent copperMyr = new Permanent(new CopperMyr());
        copperMyr.setSummoningSick(false);
        copperMyr.tap();
        gd.playerBattlefields.get(player1.getId()).add(copperMyr);

        Permanent ironMyr = new Permanent(new IronMyr());
        ironMyr.setSummoningSick(false);
        ironMyr.tap();
        gd.playerBattlefields.get(player1.getId()).add(ironMyr);

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int galvanizerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(galvanizer);
        harness.activateAbility(player1, galvanizerIndex, null, null);
        harness.passBothPriorities();

        assertThat(copperMyr.isTapped()).isFalse();
        assertThat(ironMyr.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating ability does not untap Galvanizer itself")
    void doesNotUntapSelf() {
        Permanent galvanizer = new Permanent(new MyrGalvanizer());
        galvanizer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(galvanizer);

        Permanent copperMyr = new Permanent(new CopperMyr());
        copperMyr.setSummoningSick(false);
        copperMyr.tap();
        gd.playerBattlefields.get(player1.getId()).add(copperMyr);

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int galvanizerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(galvanizer);
        harness.activateAbility(player1, galvanizerIndex, null, null);
        harness.passBothPriorities();

        // Galvanizer tapped as cost — should remain tapped
        assertThat(galvanizer.isTapped()).isTrue();
        assertThat(copperMyr.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating ability does not untap non-Myr creatures")
    void doesNotUntapNonMyr() {
        Permanent galvanizer = new Permanent(new MyrGalvanizer());
        galvanizer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(galvanizer);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int galvanizerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(galvanizer);
        harness.activateAbility(player1, galvanizerIndex, null, null);
        harness.passBothPriorities();

        // Bears are not Myr — should stay tapped
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability does not untap opponent's Myr")
    void doesNotUntapOpponentMyr() {
        Permanent galvanizer = new Permanent(new MyrGalvanizer());
        galvanizer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(galvanizer);

        Permanent opponentMyr = new Permanent(new CopperMyr());
        opponentMyr.setSummoningSick(false);
        opponentMyr.tap();
        gd.playerBattlefields.get(player2.getId()).add(opponentMyr);

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int galvanizerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(galvanizer);
        harness.activateAbility(player1, galvanizerIndex, null, null);
        harness.passBothPriorities();

        assertThat(opponentMyr.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability puts entry on the stack")
    void abilityPutsEntryOnStack() {
        Permanent galvanizer = new Permanent(new MyrGalvanizer());
        galvanizer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(galvanizer);

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int galvanizerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(galvanizer);
        harness.activateAbility(player1, galvanizerIndex, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Myr Galvanizer");
    }
}
