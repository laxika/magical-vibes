package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HelmOfChatzuk.class, GrizzlyBears.class})
class HelmOfChatzukTest extends BaseCardTest {

    @Test
    @DisplayName("Grants banding to target creature until end of turn")
    void grantsBandingToTargetCreature() {
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfChatzuk());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isTrue();
        assertThat(helm.isTapped()).isTrue();

        // The grant wears off at end of turn.
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isFalse();
    }

    @Test
    void canTargetOpponentsCreature() {
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfChatzuk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isTrue();
        assertThat(helm.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new HelmOfChatzuk());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new HelmOfChatzuk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresGenericMana() {
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfChatzuk());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helm.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isFalse();
    }
}
