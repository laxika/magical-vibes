package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraniteGripTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Granite Grip puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new GraniteGrip()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Granite Grip");
    }

    @Test
    @DisplayName("Resolving Granite Grip attaches it and grants +1/+0 per Mountain you control")
    void resolvesAndBuffsPerMountain() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new GraniteGrip()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Granite Grip")
                        && bears.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Granite Grip updates dynamically when Mountain count changes")
    void updatesDynamicallyWithMountainCount() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent grip = new Permanent(new GraniteGrip());
        grip.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(grip);

        // No mountains — no buff
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        // Add one mountain — +1/+0
        harness.addToBattlefield(player1, new Mountain());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        // Add second mountain — +2/+0
        harness.addToBattlefield(player1, new Mountain());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        // Remove all mountains — back to base
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Mountain"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Granite Grip counts Mountains controlled by aura controller, not enchanted creature's controller")
    void countsAurasControllersMountains() {
        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);

        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        Permanent grip = new Permanent(new GraniteGrip());
        grip.setAttachedTo(opponentBears.getId());
        gd.playerBattlefields.get(player1.getId()).add(grip);

        // Should count player1's 3 mountains, not player2's 1
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Granite Grip effect ends when aura leaves battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        Permanent grip = new Permanent(new GraniteGrip());
        grip.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(grip);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(grip);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Granite Grip")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GraniteGrip()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
