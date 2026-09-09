package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.cards.t.Touchstone;
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

@CardUsed({EmpyrialArmor.class, RedwoodTreefolk.class, Touchstone.class})
class EmpyrialArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving attaches and grants +1/+1 per card in the Aura controller's hand")
    void resolvesAndBoostsPerHandCard() {
        Permanent treefolk = harness.addToBattlefieldAndReturn(player1, new RedwoodTreefolk());
        gd.playerHands.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new EmpyrialArmor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, treefolk.getId());
        harness.passBothPriorities();

        // Aura left the hand; hand is empty → +0/+0.
        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(6);

        harness.setHand(player1, List.of(new RedwoodTreefolk(), new RedwoodTreefolk(), new RedwoodTreefolk()));
        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(9);
    }

    @Test
    @DisplayName("Boost tracks the Aura controller's hand, not the enchanted creature's controller")
    void boostUsesAuraControllerHand() {
        Permanent treefolk = harness.addToBattlefieldAndReturn(player2, new RedwoodTreefolk());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EmpyrialArmor());
        aura.setAttachedTo(treefolk.getId());

        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();
        harness.setHand(player1, List.of(new RedwoodTreefolk(), new RedwoodTreefolk())); // Aura controller: 2
        harness.setHand(player2, List.of(new RedwoodTreefolk(), new RedwoodTreefolk(), new RedwoodTreefolk())); // host: 3

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(8);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new RedwoodTreefolk());
        harness.addToBattlefield(player1, new Touchstone());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EmpyrialArmor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Touchstone");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
