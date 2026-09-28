package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PegasusCharger.class, GrizzlyBears.class, SuntailHawk.class, GiantSpider.class})
class PegasusChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Pegasus Charger")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent charger = addCreatureReady(player1, new PegasusCharger());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(charger.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Flying allows a creature with reach to block Pegasus Charger")
    void flyingAllowsCreatureWithReachToBlock() {
        addCreatureReady(player1, new PegasusCharger());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Flying allows another flying creature to block Pegasus Charger")
    void flyingAllowsFlyingCreatureToBlock() {
        addCreatureReady(player1, new PegasusCharger());
        Permanent blocker = addCreatureReady(player2, new PegasusCharger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("First strike defeats Suntail Hawk before it deals combat damage")
    void firstStrikeDefeatsSuntailHawkBeforeItDealsCombatDamage() {
        Permanent charger = addCreatureReady(player1, new PegasusCharger());
        addCreatureReady(player2, new SuntailHawk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(charger);
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }
}
