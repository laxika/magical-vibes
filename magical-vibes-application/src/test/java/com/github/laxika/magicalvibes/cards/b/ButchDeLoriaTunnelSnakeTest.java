package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DeathcultRogue;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkeletalSnake;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ButchDeLoriaTunnelSnake.class, DeathcultRogue.class, GrizzlyBears.class, SkeletalSnake.class})
class ButchDeLoriaTunnelSnakeTest extends BaseCardTest {

    @Test
    void attacksWithOtherRogueAndSnakeGetsBoostForEachControlledOne() {
        Permanent butch = addCreatureReady(player1, new ButchDeLoriaTunnelSnake());
        addCreatureReady(player1, new DeathcultRogue());
        addCreatureReady(player1, new SkeletalSnake());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new DeathcultRogue());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(butch.getPowerModifier()).isEqualTo(2);
        assertThat(butch.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(butch.getPowerModifier()).isZero();
        assertThat(butch.getToughnessModifier()).isZero();
    }

    @Test
    void activatedAbilityPutsMenaceCounterAndGrantsRogue() {
        addCreatureReady(player1, new ButchDeLoriaTunnelSnake());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.ROGUE)).isTrue();
    }

    @Test
    void activatedAbilityCannotTargetButch() {
        Permanent butch = addCreatureReady(player1, new ButchDeLoriaTunnelSnake());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, butch.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
