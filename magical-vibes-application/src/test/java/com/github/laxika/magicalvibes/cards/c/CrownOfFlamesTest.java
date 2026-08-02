package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrownOfFlamesTest extends BaseCardTest {

    private Permanent attachTo(Permanent host) {
        Permanent auraPerm = new Permanent(new CrownOfFlames());
        auraPerm.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);
        return auraPerm;
    }

    @Test
    @DisplayName("First ability gives enchanted creature +1/+0 until end of turn")
    void abilityBoostsPower() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachTo(bears);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power boost stacks across activations and wears off at end of turn")
    void boostStacksAndWearsOff() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachTo(bears);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability returns the Aura to its owner's hand")
    void secondAbilityReturnsAuraToHand() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachTo(bears);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> "Crown of Flames".equals(p.getCard().getName()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> "Crown of Flames".equals(c.getName()));
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new CrownOfFlames()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
