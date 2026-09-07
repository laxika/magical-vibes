package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderMobile.class, GiantSpider.class, GrizzlyBears.class})
class SpiderMobileTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives Spider-Mobile +1/+1 for each Spider you control")
    void attackingScalesWithControlledSpiders() {
        Permanent mobile = addReady(player1, new SpiderMobile());
        Permanent spider = addReady(player1, new GiantSpider());
        addReady(player1, new GiantSpider());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spider.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, mobile)).isTrue();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(mobile)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mobile)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mobile)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blocking gives Spider-Mobile +1/+1 for each Spider you control")
    void blockingScalesWithControlledSpiders() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent mobile = addReady(player2, new SpiderMobile());
        Permanent spider = addReady(player2, new GiantSpider());
        addReady(player2, new GiantSpider());

        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, spider.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, mobile)).isTrue();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                0, gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mobile)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mobile)).isEqualTo(5);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
