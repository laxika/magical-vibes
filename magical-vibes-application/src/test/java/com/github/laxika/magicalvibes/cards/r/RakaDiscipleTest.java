package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakaDisciple.class, GrizzlyBears.class})
class RakaDiscipleTest extends BaseCardTest {

    @Test
    void preventsNextDamageToCreature() {
        Permanent disciple = addReadyDisciple();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
        assertThat(disciple.isTapped()).isTrue();
    }

    @Test
    void preventsNextDamageToPlayer() {
        addReadyDisciple();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void givesTargetCreatureFlyingUntilEndOfTurn() {
        addReadyDisciple();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void flyingAbilityCannotTargetPlayer() {
        addReadyDisciple();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDisciple() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new RakaDisciple());
        disciple.setSummoningSick(false);
        return disciple;
    }
}
