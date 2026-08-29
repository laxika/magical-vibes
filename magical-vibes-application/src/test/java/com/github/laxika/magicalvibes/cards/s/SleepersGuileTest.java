package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SleepersGuileTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sleeper's Guile attaches it and gives the enchanted creature fear")
    void resolvingAttachesAndGrantsFear() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SleepersGuile()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SleepersGuile)
                .findFirst()
                .orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Sleeper's Guile returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new SleepersGuile());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sleeper's Guile");
        harness.assertNotInGraveyard(player1, "Sleeper's Guile");
        harness.assertNotOnBattlefield(player1, "Sleeper's Guile");
    }

    @Test
    @DisplayName("Sleeper's Guile cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SleepersGuile()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
