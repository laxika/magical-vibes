package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArrogantBloodlordTest extends BaseCardTest {

    @Test
    void becomesBlockedByPowerOneCreatureSchedulesSelfDestruction() {
        Permanent bloodlord = addReadyBloodlord(player1);
        bloodlord.setAttacking(true);
        addReadyMemnite(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(bloodlord.getId()));
    }

    @Test
    void blocksPowerOneCreatureSchedulesSelfDestruction() {
        Permanent memnite = addReadyMemnite(player1);
        memnite.setAttacking(true);
        Permanent bloodlord = addReadyBloodlord(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(bloodlord.getId()));
    }

    @Test
    void doesNotTriggerForPowerTwoCreature() {
        Permanent attacker = addReadySpider(player1);
        attacker.setAttacking(true);
        addReadyBloodlord(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    private Permanent addReadyBloodlord(Player player) {
        Permanent permanent = new Permanent(new ArrogantBloodlord());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyMemnite(Player player) {
        Permanent permanent = new Permanent(new Memnite());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadySpider(Player player) {
        Permanent permanent = new Permanent(new GiantSpider());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
