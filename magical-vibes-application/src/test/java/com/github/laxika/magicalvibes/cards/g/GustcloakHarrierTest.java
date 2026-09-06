package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GustcloakHarrier.class, SerraAngel.class})
class GustcloakHarrierTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the becomes-blocked trigger untaps and removes the Harrier from combat")
    void acceptingBecomesBlockedTriggerUntapsAndRemovesFromCombat() {
        Permanent harrier = addHarrier();
        harrier.tap();
        Permanent blocker = addCreatureReady(player2);

        harrier.setAttacking(true);
        harrier.setAttackTarget(player2.getId());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harrier.isTapped()).isFalse();
        assertThat(harrier.isAttacking()).isFalse();
        assertThat(harrier.getAttackTarget()).isNull();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining the becomes-blocked trigger leaves the Harrier in combat")
    void decliningBecomesBlockedTriggerLeavesItInCombat() {
        Permanent harrier = addHarrier();
        harrier.tap();
        Permanent blocker = addCreatureReady(player2);

        harrier.setAttacking(true);
        harrier.setAttackTarget(player2.getId());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harrier.isTapped()).isTrue();
        assertThat(harrier.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addHarrier() {
        return addCreatureReady(player1, new GustcloakHarrier());
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new SerraAngel());
    }
}
