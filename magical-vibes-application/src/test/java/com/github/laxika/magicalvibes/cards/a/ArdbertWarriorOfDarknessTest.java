package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuanYuSaintedWarrior;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArdbertWarriorOfDarkness.class, GuanYuSaintedWarrior.class, GrizzlyBears.class,
        SerraAngel.class, ScatheZombies.class})
class ArdbertWarriorOfDarknessTest extends BaseCardTest {

    private BattlefieldCards addBattlefieldCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent ardbert = harness.addToBattlefieldAndReturn(player1, new ArdbertWarriorOfDarkness());
        Permanent guanYu = harness.addToBattlefieldAndReturn(player1, new GuanYuSaintedWarrior());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        return new BattlefieldCards(ardbert, guanYu, bears);
    }

    @Test
    @DisplayName("A white spell boosts legendary creatures and grants them vigilance")
    void whiteSpellBoostsLegendaryCreatures() {
        BattlefieldCards cards = addBattlefieldCards();

        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(cards.ardbert.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(cards.guanYu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(cards.bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, cards.ardbert, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, cards.guanYu, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, cards.bears, Keyword.VIGILANCE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cards.ardbert, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, cards.guanYu, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A black spell boosts legendary creatures and grants them menace")
    void blackSpellBoostsLegendaryCreatures() {
        BattlefieldCards cards = addBattlefieldCards();

        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(cards.ardbert.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(cards.guanYu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(cards.bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, cards.ardbert, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, cards.guanYu, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, cards.bears, Keyword.MENACE)).isFalse();
    }

    private record BattlefieldCards(Permanent ardbert, Permanent guanYu, Permanent bears) {
    }
}
