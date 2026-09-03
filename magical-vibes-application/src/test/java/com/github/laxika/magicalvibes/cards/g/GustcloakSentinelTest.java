package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GustcloakSentinel.class, SerraAngel.class})
class GustcloakSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the becomes-blocked trigger untaps and removes the Sentinel from combat")
    void acceptingBecomesBlockedTriggerUntapsAndRemovesFromCombat() {
        Permanent sentinel = addSentinel();
        sentinel.tap();
        Permanent blocker = addCreatureReady(player2);

        sentinel.setAttacking(true);
        sentinel.setAttackTarget(player2.getId());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sentinel.isTapped()).isFalse();
        assertThat(sentinel.isAttacking()).isFalse();
        assertThat(sentinel.getAttackTarget()).isNull();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining the becomes-blocked trigger leaves the Sentinel in combat")
    void decliningBecomesBlockedTriggerLeavesItInCombat() {
        Permanent sentinel = addSentinel();
        sentinel.tap();
        Permanent blocker = addCreatureReady(player2);

        sentinel.setAttacking(true);
        sentinel.setAttackTarget(player2.getId());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(sentinel.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addSentinel() {
        return addCreatureReady(player1, new GustcloakSentinel());
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new SerraAngel());
    }
}
