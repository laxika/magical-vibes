package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoggCannon.class, LowlandGiant.class})
class MoggCannonTest extends BaseCardTest {

    @Test
    @DisplayName("Grants +1/+0 and flying to target creature you control")
    void boostsAndGrantsFlying() {
        Permanent cannon = harness.addToBattlefieldAndReturn(player1, new MoggCannon());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new LowlandGiant());

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, giant.getId());
        assertThat(after.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(3);
        assertThat(cannon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Target creature is destroyed at the beginning of the next end step")
    void destroysTargetAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addToBattlefield(player1, new MoggCannon());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new LowlandGiant());

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lowland Giant");

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Lowland Giant");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lowland Giant");
        harness.assertInGraveyard(player1, "Lowland Giant");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new MoggCannon());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new LowlandGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentGiant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent you control")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new MoggCannon());
        Permanent otherCannon = harness.addToBattlefieldAndReturn(player1, new MoggCannon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherCannon.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
