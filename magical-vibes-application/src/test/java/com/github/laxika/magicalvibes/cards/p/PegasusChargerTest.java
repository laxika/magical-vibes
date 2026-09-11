package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.s.SkirgeFamiliar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PegasusCharger.class, GorillaWarrior.class, SkirgeFamiliar.class})
class PegasusChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Pegasus Charger")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent charger = addCreatureReady(player1, new PegasusCharger());
        addCreatureReady(player2, new GorillaWarrior());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(charger.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Flying allows another flying creature to block Pegasus Charger")
    void flyingAllowsFlyingCreatureToBlock() {
        addCreatureReady(player1, new PegasusCharger());
        Permanent blocker = addCreatureReady(player2, new PegasusCharger());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("First strike defeats Skirge Familiar before it deals combat damage")
    void firstStrikeDefeatsSkirgeFamiliarBeforeItDealsCombatDamage() {
        Permanent charger = addCreatureReady(player1, new PegasusCharger());
        addCreatureReady(player2, new SkirgeFamiliar());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(charger);
        harness.assertInGraveyard(player2, "Skirge Familiar");
    }
}
