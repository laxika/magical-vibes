package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SleekSchooner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AetherMeltdownTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature, gives two energy counters, and weakens it")
    void entersAndWeakensCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castAetherMeltdown(bears);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Aether Meltdown")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Can enchant a noncreature Vehicle")
    void canEnchantVehicle() {
        Permanent schooner = harness.addToBattlefieldAndReturn(player2, new SleekSchooner());

        harness.setHand(player1, List.of(new AetherMeltdown()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, schooner.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature non-Vehicle permanent")
    void cannotEnchantOtherPermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new AetherMeltdown()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    @DisplayName("The weakening ends when Aether Meltdown leaves the battlefield")
    void weakeningEndsWhenRemoved() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castAetherMeltdown(bears);

        Permanent aura = findPermanent(player1, "Aether Meltdown");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    private void castAetherMeltdown(Permanent target) {
        harness.setHand(player1, List.of(new AetherMeltdown()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
