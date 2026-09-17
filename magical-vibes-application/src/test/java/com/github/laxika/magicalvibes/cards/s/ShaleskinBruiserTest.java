package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShaleskinBruiser.class, BarkhideMauler.class, ElvishWarrior.class})
class ShaleskinBruiserTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+0 for each other attacking Beast")
    void boostsForOtherAttackingBeasts() {
        Permanent bruiser = addCreatureReady(player1, new ShaleskinBruiser());
        addCreatureReady(player1, new BarkhideMauler());
        addCreatureReady(player1, new BarkhideMauler());
        addCreatureReady(player1, new ElvishWarrior());
        int basePower = gqs.getEffectivePower(gd, bruiser);
        int baseToughness = gqs.getEffectiveToughness(gd, bruiser);

        declareAttackers(List.of(0, 1, 3));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bruiser)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, bruiser)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Does not boost when no other Beast attacks")
    void doesNotBoostWithoutAnotherAttackingBeast() {
        Permanent bruiser = addCreatureReady(player1, new ShaleskinBruiser());
        int basePower = gqs.getEffectivePower(gd, bruiser);
        int baseToughness = gqs.getEffectiveToughness(gd, bruiser);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bruiser)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, bruiser)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Gets +6/+0 for two other attacking Beasts")
    void countsEachOtherAttackingBeast() {
        Permanent bruiser = addCreatureReady(player1, new ShaleskinBruiser());
        addCreatureReady(player1, new BarkhideMauler());
        addCreatureReady(player1, new BarkhideMauler());
        int basePower = gqs.getEffectivePower(gd, bruiser);
        int baseToughness = gqs.getEffectiveToughness(gd, bruiser);

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bruiser)).isEqualTo(basePower + 6);
        assertThat(gqs.getEffectiveToughness(gd, bruiser)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bruiser = addCreatureReady(player1, new ShaleskinBruiser());
        addCreatureReady(player1, new BarkhideMauler());
        int basePower = gqs.getEffectivePower(gd, bruiser);
        int baseToughness = gqs.getEffectiveToughness(gd, bruiser);

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bruiser)).isEqualTo(basePower + 3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, bruiser)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, bruiser)).isEqualTo(baseToughness);
    }

}
