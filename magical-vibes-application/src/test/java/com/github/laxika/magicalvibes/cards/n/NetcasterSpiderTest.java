package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.w.WelkinTern;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NetcasterSpider.class, RuneclawBear.class, WelkinTern.class})
class NetcasterSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature with flying triggers +2/+0 boost")
    void blockingFlyingCreatureTriggersBoost() {
        Permanent spider = addReadySpider(player2);
        addReadyAttacker(player1, new WelkinTern());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(3);
    }

    @Test
    @DisplayName("Blocking a creature without flying does not trigger boost")
    void blockingNonFlyingCreatureDoesNotTrigger() {
        Permanent spider = addReadySpider(player2);
        addReadyAttacker(player1, new RuneclawBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent spider = addReadySpider(player2);
        addReadyAttacker(player1, new WelkinTern());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(4);

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(4);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the Spider blocking a flying creature gets the boost after its trigger resolves")
    void boostAppliesOnlyToBlockingSpiderAfterResolution() {
        Permanent blocker = addReadySpider(player2);
        Permanent otherSpider = addReadySpider(player2);
        addReadyAttacker(player1, new WelkinTern());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherSpider)).isEqualTo(2);
    }

    private Permanent addReadySpider(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new NetcasterSpider());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        return perm;
    }
}
