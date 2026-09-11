package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZimoneAllQuestioning.class, Forest.class})
class ZimoneAllQuestioningTest extends BaseCardTest {

    @Test
    void createsLegendaryFractalWithCountersEqualToControlledLands() {
        harness.addToBattlefield(player1, new ZimoneAllQuestioning());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        resolveControllerEndStep();

        Permanent primo = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Primo, the Indivisible".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(primo.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(primo.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(primo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(primo.getEffectivePower()).isEqualTo(2);
        assertThat(primo.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForACompositeNumberOfLands() {
        harness.addToBattlefield(player1, new ZimoneAllQuestioning());
        for (int i = 0; i < 4; i++) {
            harness.enterBattlefieldAndReturn(player1, new Forest());
        }

        resolveControllerEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && "Primo, the Indivisible".equals(permanent.getCard().getName()));
    }

    @Test
    void doesNotResolveIfTheLandCountStopsBeingPrime() {
        harness.addToBattlefield(player1, new ZimoneAllQuestioning());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().hasType(CardType.LAND));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && "Primo, the Indivisible".equals(permanent.getCard().getName()));
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
