package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.p.PaleBears;
import com.github.laxika.magicalvibes.cards.s.Seraph;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoollySpider.class, Seraph.class, PaleBears.class})
class WoollySpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature with flying triggers +0/+2 boost")
    void blockingFlyingCreatureTriggersBoost() {
        Permanent spider = addCreatureReady(player2, new WoollySpider());
        addReadyAttacker(player1, new Seraph());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isEqualTo(0);
        assertThat(spider.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blocking a creature without flying does not trigger boost")
    void blockingNonFlyingCreatureDoesNotTrigger() {
        Permanent spider = addCreatureReady(player2, new WoollySpider());
        addReadyAttacker(player1, new PaleBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(spider.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent spider = addCreatureReady(player2, new WoollySpider());
        addReadyAttacker(player1, new Seraph());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(spider.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.CLEANUP);
        spider.resetModifiers();

        assertThat(spider.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(3);
    }

    private void addReadyAttacker(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.setAttacking(true);
    }
}
