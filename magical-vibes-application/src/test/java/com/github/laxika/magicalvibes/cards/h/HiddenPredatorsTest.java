package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HiddenPredatorsTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 4/4 Beast creature when an opponent controls a creature with power 4 or greater")
    void becomesBeastCreatureWhenOpponentControlsCreatureWithPowerAtLeastFour() {
        Permanent hiddenPredators = harness.addToBattlefieldAndReturn(player1, new HiddenPredators());
        harness.addToBattlefield(player2, new AvatarOfMight());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenPredators)).isTrue();
        assertThat(gqs.isEnchantment(gd, hiddenPredators)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hiddenPredators)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hiddenPredators)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hiddenPredators)).containsExactly(CardSubtype.BEAST);
    }

    @Test
    @DisplayName("Does not trigger when an opponent controls no creature with power 4 or greater")
    void doesNotTriggerBelowPowerThreshold() {
        Permanent hiddenPredators = harness.addToBattlefieldAndReturn(player1, new HiddenPredators());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isEnchantment(gd, hiddenPredators)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenPredators)).isFalse();
    }

    @Test
    @DisplayName("A qualifying creature controlled by this card's controller does not trigger it")
    void doesNotTriggerForControllerCreature() {
        Permanent hiddenPredators = harness.addToBattlefieldAndReturn(player1, new HiddenPredators());
        harness.addToBattlefield(player1, new AvatarOfMight());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isEnchantment(gd, hiddenPredators)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenPredators)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger again after becoming a creature")
    void doesNotTriggerAgainAfterBecomingCreature() {
        Permanent hiddenPredators = harness.addToBattlefieldAndReturn(player1, new HiddenPredators());
        harness.addToBattlefield(player2, new AvatarOfMight());

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hiddenPredators)).isTrue();

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, hiddenPredators)).isTrue();
    }
}
