package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BruteSuit;
import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkkiWarPaint.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class,
        BruteSuit.class, JukaiTrainee.class})
class AkkiWarPaintTest extends BaseCardTest {

    @Test
    @DisplayName("Akki War Paint gives an enchanted creature +2/+1")
    void enchantedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachAkkiWarPaint(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Akki War Paint can enchant an artifact without boosting it")
    void canEnchantArtifactWithoutBoostingIt() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new AkkiWarPaint()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Akki War Paint")
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Akki War Paint cannot enchant a nonartifact noncreature permanent")
    void cannotEnchantForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new AkkiWarPaint()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @CardUsed({AkkiWarPaint.class, BruteSuit.class, JukaiTrainee.class})
    void enchantedVehicleGetsBoostOnlyWhileItIsACreature() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new BruteSuit());
        harness.addToBattlefield(player1, new JukaiTrainee());
        harness.setHand(player1, List.of(new AkkiWarPaint()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, vehicle.getId());
        harness.passBothPriorities();

        Permanent aura = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Akki War Paint"));
        assertThat(aura.getAttachedTo()).isEqualTo(vehicle.getId());
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(aura.getAttachedTo()).isEqualTo(vehicle.getId());
        harness.assertOnBattlefield(player1, "Akki War Paint");

    }

    private void attachAkkiWarPaint(Permanent enchantedPermanent) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AkkiWarPaint());
        aura.setAttachedTo(enchantedPermanent.getId());
    }
}
