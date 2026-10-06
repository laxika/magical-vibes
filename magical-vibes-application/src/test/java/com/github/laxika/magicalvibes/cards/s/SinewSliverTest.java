package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PoulticeSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SinewSliver.class, PoulticeSliver.class, SerraSphinx.class})
class SinewSliverTest extends BaseCardTest {

    @Test
    void boostsSelf() {
        Permanent sliver = addCreatureReady(player1, new SinewSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(2);
    }

    @Test
    void boostsOtherSliverYouControl() {
        Permanent otherSliver = addCreatureReady(player1, new PoulticeSliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);

        addCreatureReady(player1, new SinewSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness + 1);
    }

    @Test
    void boostsOpponentsSlivers() {
        Permanent opponentSliver = addCreatureReady(player2, new PoulticeSliver());
        int basePower = gqs.getEffectivePower(gd, opponentSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, opponentSliver);

        addCreatureReady(player1, new SinewSliver());

        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(baseToughness + 1);
    }

    @Test
    void doesNotBoostNonSliverCreatures() {
        Permanent creature = addCreatureReady(player1, new SerraSphinx());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);

        addCreatureReady(player1, new SinewSliver());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness);
    }

    @Test
    void multipleSinewSliversStack() {
        Permanent first = addCreatureReady(player1, new SinewSliver());
        Permanent second = addCreatureReady(player1, new SinewSliver());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void boostsSliverEnteringAfterSource() {
        harness.enterBattlefieldAndReturn(player1, new SinewSliver());

        Permanent laterSliver = harness.enterBattlefieldAndReturn(player2, new PoulticeSliver());

        assertThat(gqs.getEffectivePower(gd, laterSliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, laterSliver)).isEqualTo(3);
    }

    @Test
    void opposingSinewSliversStackAndOnlyDepartingSourcesBonusEnds() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new SinewSliver());
        Permanent second = harness.enterBattlefieldAndReturn(player2, new SinewSliver());
        Permanent otherSliver = harness.enterBattlefieldAndReturn(player2, new PoulticeSliver());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(4);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, first));

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(3);
    }

    @Test
    void stopsBoostingWhenSourceLeavesBattlefield() {
        Permanent otherSliver = addCreatureReady(player1, new PoulticeSliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);
        Permanent source = addCreatureReady(player1, new SinewSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness + 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness);
    }
}
