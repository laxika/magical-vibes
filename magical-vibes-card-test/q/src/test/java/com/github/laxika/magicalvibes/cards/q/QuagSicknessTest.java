package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.d.DragonsClaw;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuagSickness.class, RuneclawBear.class, DragonsClaw.class, Swamp.class, Plains.class})
class QuagSicknessTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Quag Sickness puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new QuagSickness()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(QuagSickness.class);
    }

    @Test
    @DisplayName("Resolving Quag Sickness attaches it and grants -1/-1 per Swamp you control")
    void resolvesAndDebuffsPerSwamp() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new QuagSickness()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Quag Sickness")
                        && bears.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Quag Sickness updates dynamically when Swamp count changes")
    void updatesDynamicallyWithSwampCount() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent sickness = harness.addToBattlefieldAndReturn(player1, new QuagSickness());
        sickness.setAttachedTo(bears.getId());

        // No swamps — no debuff
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        // Add one swamp — -1/-1
        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);

        // Add second swamp — -2/-2
        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(0);

        // Remove all swamps — back to base
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Swamp"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Quag Sickness counts Swamps controlled by aura controller, not enchanted creature's controller")
    void countsAurasControllersSwamps() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        Permanent sickness = harness.addToBattlefieldAndReturn(player1, new QuagSickness());
        sickness.setAttachedTo(opponentBears.getId());

        // Should count player1's 3 swamps, not player2's 1
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(-1);
    }

    @Test
    @DisplayName("Quag Sickness effect ends when aura leaves battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        Permanent sickness = harness.addToBattlefieldAndReturn(player1, new QuagSickness());
        sickness.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(0);

        gd.playerBattlefields.get(player1.getId()).remove(sickness);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Quag Sickness")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new DragonsClaw());
        harness.setHand(player1, List.of(new QuagSickness()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        Permanent artifact = findPermanent(player1, "Dragon's Claw");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
    @Test
    @DisplayName("Non-Swamp lands do not increase the penalty")
    void ignoresNonSwampLands() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new QuagSickness()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Quag Sickness");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Zero toughness puts the creature and its Aura into their owners' graveyards")
    void lethalPenaltyRemovesCreatureAndAura() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new QuagSickness()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Quag Sickness");
        harness.assertInGraveyard(player1, "Quag Sickness");
    }

    @Test
    @DisplayName("An Aura whose target has left the battlefield does not resolve")
    void targetLeavingBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new QuagSickness()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, bears.getId());

        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerGraveyards.get(player2.getId()).add(bears.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Quag Sickness");
        harness.assertInGraveyard(player1, "Quag Sickness");
    }
}
