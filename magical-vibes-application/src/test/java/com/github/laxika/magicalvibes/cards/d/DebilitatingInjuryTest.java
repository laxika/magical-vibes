package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AltarOfTheBrood;
import com.github.laxika.magicalvibes.cards.v.ValleyDasher;
import com.github.laxika.magicalvibes.cards.s.SummitProwler;
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

@CardUsed({DebilitatingInjury.class, ValleyDasher.class, SummitProwler.class, AltarOfTheBrood.class})
class DebilitatingInjuryTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Debilitating Injury targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ValleyDasher());

        harness.setHand(player1, List.of(new DebilitatingInjury()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Debilitating Injury");
    }

    @Test
    @DisplayName("Resolving Debilitating Injury attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new SummitProwler());

        harness.setHand(player1, List.of(new DebilitatingInjury()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Debilitating Injury")
                        && giant.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets -2/-2")
    void enchantedCreatureGetsDebuff() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new SummitProwler());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DebilitatingInjury());
        aura.setAttachedTo(giant.getId());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature returns to base stats when Debilitating Injury is removed")
    void effectsStopWhenRemoved() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new SummitProwler());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DebilitatingInjury());
        aura.setAttachedTo(giant.getId());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Debilitating Injury kills a 2/2 creature")
    void killsTwoToughnessCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ValleyDasher());

        harness.setHand(player1, List.of(new DebilitatingInjury()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Valley Dasher");
        harness.assertInGraveyard(player1, "Valley Dasher");
        harness.assertInGraveyard(player1, "Debilitating Injury");
        harness.assertNotOnBattlefield(player1, "Debilitating Injury");
    }

    @Test
    @DisplayName("Debilitating Injury fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ValleyDasher());

        harness.setHand(player1, List.of(new DebilitatingInjury()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Debilitating Injury");
        harness.assertNotOnBattlefield(player1, "Debilitating Injury");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Debilitating Injury")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new ValleyDasher());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AltarOfTheBrood());
        harness.setHand(player1, List.of(new DebilitatingInjury()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Only the enchanted opposing creature gets -2/-2")
    void debuffsOnlyEnchantedOpposingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SummitProwler());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SummitProwler());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SummitProwler());
        harness.setHand(player1, List.of(new DebilitatingInjury()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(3);
        assertThat(findPermanent(player1, "Debilitating Injury").getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Two Debilitating Injuries combine and both Auras die with the creature")
    void multipleCopiesCombine() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SummitProwler());
        harness.setHand(player1, List.of(new DebilitatingInjury(), new DebilitatingInjury()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Summit Prowler");
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Summit Prowler");
        harness.assertInGraveyard(player2, "Summit Prowler");
        harness.assertNotOnBattlefield(player1, "Debilitating Injury");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof DebilitatingInjury).hasSize(2);
    }
}
