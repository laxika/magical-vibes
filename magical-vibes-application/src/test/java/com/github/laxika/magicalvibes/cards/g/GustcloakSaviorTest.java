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

@CardUsed({GustcloakSavior.class, SerraAngel.class})
class GustcloakSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the becomes-blocked trigger untaps and removes the Savior from combat")
    void acceptingBecomesBlockedTriggerUntapsAndRemovesFromCombat() {
        Permanent savior = addSavior();
        savior.tap();
        Permanent blocker = addCreatureReady(player2);

        savior.setAttacking(true);
        savior.setAttackTarget(player2.getId());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(savior.isTapped()).isFalse();
        assertThat(savior.isAttacking()).isFalse();
        assertThat(savior.getAttackTarget()).isNull();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining the becomes-blocked trigger leaves the Savior in combat")
    void decliningBecomesBlockedTriggerLeavesItInCombat() {
        Permanent savior = addSavior();
        savior.tap();
        Permanent blocker = addCreatureReady(player2);

        savior.setAttacking(true);
        savior.setAttackTarget(player2.getId());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(savior.isTapped()).isTrue();
        assertThat(savior.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addSavior() {
        return addCreatureReady(player1, new GustcloakSavior());
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new SerraAngel());
    }
}
