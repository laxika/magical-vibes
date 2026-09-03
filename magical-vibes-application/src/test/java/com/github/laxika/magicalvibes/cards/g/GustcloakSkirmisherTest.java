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

@CardUsed({GustcloakSkirmisher.class, SerraAngel.class})
class GustcloakSkirmisherTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the becomes-blocked trigger untaps and removes the Skirmisher from combat")
    void acceptingBecomesBlockedTriggerUntapsAndRemovesFromCombat() {
        Permanent skirmisher = addSkirmisher();
        skirmisher.tap();
        Permanent blocker = addCreatureReady(player2);

        skirmisher.setAttacking(true);
        skirmisher.setAttackTarget(player2.getId());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(skirmisher.isTapped()).isFalse();
        assertThat(skirmisher.isAttacking()).isFalse();
        assertThat(skirmisher.getAttackTarget()).isNull();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining the becomes-blocked trigger leaves the Skirmisher in combat")
    void decliningBecomesBlockedTriggerLeavesItInCombat() {
        Permanent skirmisher = addSkirmisher();
        skirmisher.tap();
        Permanent blocker = addCreatureReady(player2);

        skirmisher.setAttacking(true);
        skirmisher.setAttackTarget(player2.getId());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(skirmisher.isTapped()).isTrue();
        assertThat(skirmisher.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addSkirmisher() {
        return addCreatureReady(player1, new GustcloakSkirmisher());
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new SerraAngel());
    }
}
