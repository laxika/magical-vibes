package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrokenFall.class, LotusPetal.class, LowlandGiant.class})
class BrokenFallTest extends BaseCardTest {

    @Test
    @DisplayName("Returns to hand as a cost and puts a regeneration shield on the target creature")
    void bouncesItselfAndRegeneratesTarget() {
        harness.addToBattlefield(player1, new BrokenFall());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new LowlandGiant());

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(giant.getRegenerationShield()).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Broken Fall");
        harness.assertInHand(player1, "Broken Fall");
    }

    @Test
    @DisplayName("The regeneration shield saves the target from lethal damage")
    void regenerationShieldSavesTarget() {
        harness.addToBattlefield(player1, new BrokenFall());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new LowlandGiant());

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();

        giant.setMarkedDamage(100);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player2, "Lowland Giant");
        assertThat(giant.isTapped()).isTrue();
        assertThat(giant.getMarkedDamage()).isZero();
        assertThat(giant.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new BrokenFall());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LotusPetal());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
