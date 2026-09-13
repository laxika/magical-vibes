package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ClawsOfGix;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Despondency.class, GorillaWarrior.class, ClawsOfGix.class})
class DespondencyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Despondency attaches it and gives the enchanted creature -2/-0")
    void resolvingAttachesAndShrinksCreature() {
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        harness.setHand(player1, List.of(new Despondency()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, gorilla.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Despondency");
        assertThat(aura.getAttachedTo()).isEqualTo(gorilla.getId());
        assertThat(gqs.getEffectivePower(gd, gorilla)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gorilla)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Despondency is put into a graveyard from the battlefield, it returns to its owner's hand")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Despondency());
        aura.setAttachedTo(gorilla.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Despondency");
        harness.assertNotInGraveyard(player1, "Despondency");
        harness.assertNotOnBattlefield(player1, "Despondency");
        assertThat(gqs.getEffectivePower(gd, gorilla)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gorilla)).isEqualTo(2);
    }

    @Test
    @DisplayName("Despondency cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ClawsOfGix());
        harness.setHand(player1, List.of(new Despondency()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
