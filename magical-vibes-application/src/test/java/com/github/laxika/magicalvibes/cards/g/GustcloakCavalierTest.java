package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GustcloakCavalier.class, GrizzlyBears.class})
class GustcloakCavalierTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers to tap a target creature")
    void attackingMayTapTargetCreature() {
        Permanent cavalier = addCavalier();
        Permanent bears = addCreatureReady(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the target creature untapped")
    void decliningAttackTriggerLeavesTargetUntapped() {
        addCavalier();
        Permanent bears = addCreatureReady(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Accepting the becomes-blocked trigger untaps and removes the Cavalier from combat")
    void acceptingBecomesBlockedTriggerUntapsAndRemovesFromCombat() {
        Permanent cavalier = addCavalier();
        cavalier.tap();
        Permanent blocker = addCreatureReady(player2);

        cavalier.setAttacking(true);
        cavalier.setAttackTarget(player2.getId());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cavalier.isTapped()).isFalse();
        assertThat(cavalier.isAttacking()).isFalse();
        assertThat(cavalier.getAttackTarget()).isNull();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining the becomes-blocked trigger leaves the Cavalier in combat")
    void decliningBecomesBlockedTriggerLeavesItInCombat() {
        Permanent cavalier = addCavalier();
        cavalier.tap();
        Permanent blocker = addCreatureReady(player2);

        cavalier.setAttacking(true);
        cavalier.setAttackTarget(player2.getId());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(cavalier.isTapped()).isTrue();
        assertThat(cavalier.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addCavalier() {
        return addCreatureReady(player1, new GustcloakCavalier());
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
