package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HighSentinelsOfArashinTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each other countered creature you control")
    void scalesWithOtherCounteredCreaturesYouControl() {
        Permanent sentinels = addCreatureReady(player1, new HighSentinelsOfArashin());
        Permanent counteredCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears()).setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, sentinels)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentinels)).isEqualTo(5);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on the targeted creature")
    void putsCounterOnTargetCreature() {
        Permanent sentinels = addCreatureReady(player1, new HighSentinelsOfArashin());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        activateAbility(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, sentinels)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentinels)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a land with its activated ability")
    void cannotTargetLand() {
        addCreatureReady(player1, new HighSentinelsOfArashin());
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(forest);
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void activateAbility(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addManaForAbility();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
