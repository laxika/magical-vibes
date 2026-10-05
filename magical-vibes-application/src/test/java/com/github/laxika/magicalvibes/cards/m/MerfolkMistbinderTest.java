package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkMistbinder.class, CoralMerfolk.class, GrizzlyBears.class})
class MerfolkMistbinderTest extends BaseCardTest {

    @Test
    @DisplayName("Other Merfolk you control get +1/+1")
    void buffsOtherMerfolkYouControl() {
        harness.addToBattlefield(player1, new MerfolkMistbinder());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Merfolk Mistbinder does not buff itself")
    void doesNotBuffItself() {
        Permanent mistbinder = harness.addToBattlefieldAndReturn(player1, new MerfolkMistbinder());

        assertThat(gqs.getEffectivePower(gd, mistbinder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mistbinder)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Merfolk creatures")
    void doesNotBuffNonMerfolk() {
        harness.addToBattlefield(player1, new MerfolkMistbinder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff an opponent's Merfolk")
    void doesNotBuffOpponentsMerfolk() {
        harness.addToBattlefield(player1, new MerfolkMistbinder());
        Permanent opponentMerfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());

        assertThat(gqs.getEffectivePower(gd, opponentMerfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentMerfolk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Mistbinder receives the boosts from the other Mistbinders")
    void multipleMistbindersBoostEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MerfolkMistbinder());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MerfolkMistbinder());
        Permanent third = harness.enterBattlefieldAndReturn(player1, new MerfolkMistbinder());

        for (Permanent mistbinder : java.util.List.of(first, second, third)) {
            assertThat(gqs.getEffectivePower(gd, mistbinder)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, mistbinder)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("A Mistbinder's boost ends when it dies, making damage lethal to another Mistbinder")
    void losingMistbinderMakesDamageLethal() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MerfolkMistbinder());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MerfolkMistbinder());

        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        first.setMarkedDamage(3);
        second.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first.getCard(), second.getCard());
    }
}
