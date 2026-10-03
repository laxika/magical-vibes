package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AureliaTheLawAbove;
import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.s.SkyknightLegionnaire;
import com.github.laxika.magicalvibes.cards.s.SparkTrooper;
import com.github.laxika.magicalvibes.cards.s.SwiftbladeVindicator;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TajicLegionsValor.class, BorosRecruit.class, SwiftbladeVindicator.class,
        SkyknightLegionnaire.class, SparkTrooper.class, AureliaTheLawAbove.class})
class TajicLegionsValorTest extends BaseCardTest {

    @Test
    void firstCombatPutsACounterAndConjuresAOneManaSpellbookCreature() {
        Permanent tajic = addCreatureReady(player1, new TajicLegionsValor());

        resolveBeginningOfCombat(player1);

        assertThat(tajic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Permanent recruit = findPermanent(player1, "Boros Recruit");
        assertThat(recruit.getCard().getManaValue()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.HASTE)).isTrue();
        assertThat(recruit.isMustAttackThisCombat()).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void secondCounterConjuresTheTwoManaSpellbookCreatureAndHasteExpires() {
        Permanent tajic = addCreatureReady(player1, new TajicLegionsValor());
        tajic.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveBeginningOfCombat(player1);

        assertThat(tajic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Swiftblade Vindicator")).hasSize(1);
        Permanent vindicator = findPermanent(player1, "Swiftblade Vindicator");
        assertThat(gqs.hasKeyword(gd, vindicator, Keyword.HASTE)).isTrue();
        assertThat(findPermanents(player1, "Boros Recruit")).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, vindicator, Keyword.HASTE)).isFalse();
    }

    private void resolveBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
