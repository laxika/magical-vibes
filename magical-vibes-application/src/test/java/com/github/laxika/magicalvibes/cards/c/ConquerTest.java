package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Conquer.class, Disenchant.class, Forest.class, BalduvianBears.class})
class ConquerTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Conquer targeting a land puts it on the stack")
    void castingPutsOnStack() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Conquer()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0, land.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(land.getId());
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving Conquer steals the enchanted land")
    void resolvingStealsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Conquer()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        // Land should now be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(land.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(land.getId()));

        // Conquer aura should be attached to the land under player1's control
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Conquer")
                        && p.isAttached()
                        && p.getAttachedTo().equals(land.getId()));
    }

    @Test
    @DisplayName("Conquer fizzles if its target land leaves before resolution")
    void fizzlesIfTargetLandLeavesBeforeResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Conquer()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0, land.getId());
        gd.playerBattlefields.get(player2.getId()).remove(land);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Conquer");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Conquer"));
    }

    @Test
    @DisplayName("Conquer can target a land its caster controls")
    void canTargetOwnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new Conquer()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0, land.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(land.getId());
    }

    @Test
    @DisplayName("Land returns to its owner when Conquer is destroyed")
    void landReturnsWhenConquerDestroyed() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Conquer()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        Permanent conquerPerm = findPermanent(player1, "Conquer");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, conquerPerm.getId());
        harness.passBothPriorities();

        // Land should return to player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(land.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(land.getId()));
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a non-land permanent with Conquer")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player2, new Forest()); // valid target so the spell is playable
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        harness.setHand(player1, List.of(new Conquer()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
