package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LukeCagePowerMan.class, GrizzlyBears.class})
class LukeCagePowerManTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone gives Luke +2/+0 and indestructible until end of turn")
    void attackingAloneGrantsBoostAndIndestructible() {
        Permanent luke = addCreatureReady(player1, new LukeCagePowerMan());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, luke)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, luke)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, luke, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, luke)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, luke)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, luke, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Attacking with another creature does not trigger Luke's ability")
    void doesNotTriggerWhenAttackingWithAnotherCreature() {
        Permanent luke = addCreatureReady(player1, new LukeCagePowerMan());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, luke)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, luke)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, luke, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
