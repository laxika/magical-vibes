package com.github.laxika.magicalvibes.cards.l;

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

class LightningDiademTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and the ETB trigger deals 2 damage to a player")
    void boostsEnchantedCreatureAndDamagesPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        castLightningDiadem(creature.getId());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The ETB trigger can damage the enchanted creature")
    void damagesEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castLightningDiadem(creature.getId());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ETB damage target must be a creature, planeswalker, or player")
    void rejectsInvalidEtbTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new LightningDiadem()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be any target");
    }

    private void castLightningDiadem(java.util.UUID enchantTargetId) {
        harness.setHand(player1, List.of(new LightningDiadem()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castEnchantment(player1, 0, enchantTargetId);
    }
}
