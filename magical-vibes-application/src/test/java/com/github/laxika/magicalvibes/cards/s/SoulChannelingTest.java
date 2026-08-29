package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulChannelingTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life regenerates the enchanted creature")
    void regeneratesEnchantedCreature() {
        Permanent aura = enchantBears();
        Permanent bears = enchantedCreature(aura);

        harness.activateAbility(player1, indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot pay the regeneration cost without enough life")
    void cannotPayRegenerationCostWithoutEnoughLife() {
        Permanent aura = enchantBears();
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SoulChanneling()));

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent enchantBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulChanneling()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Soul Channeling");
    }

    private Permanent enchantedCreature(Permanent aura) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(aura.getAttachedTo()))
                .findFirst()
                .orElseThrow();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
