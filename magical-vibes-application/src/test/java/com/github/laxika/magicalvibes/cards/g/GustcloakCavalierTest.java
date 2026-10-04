package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GustcloakCavalier.class, AshcoatBear.class, BenalishCavalry.class})
class GustcloakCavalierTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers to tap a target creature")
    void attackingMayTapTargetCreature() {
        addCavalier();
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
        resolveAllTriggers();
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
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(cavalier.isTapped()).isTrue();
        assertThat(cavalier.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1 until end of turn")
    void flankingWeakensNonFlankingBlocker() {
        Permanent cavalier = addCavalier();
        cavalier.setAttacking(true);
        Permanent blocker = addCreatureReady(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flanking does not weaken a blocker that also has flanking")
    void flankingLeavesFlankingBlockerUntouched() {
        Permanent cavalier = addCavalier();
        cavalier.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BenalishCavalry());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures still creates one may choice")
    void multipleBlockersCreateOneBecomesBlockedChoice() {
        Permanent cavalier = addCavalier();
        cavalier.tap();
        cavalier.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new BenalishCavalry());
        Permanent secondBlocker = addCreatureReady(player2, new BenalishCavalry());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cavalier.isTapped()).isFalse();
        assertThat(cavalier.isAttacking()).isFalse();
        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger can tap a creature controlled by its controller")
    void attackingMayTapFriendlyCreature() {
        addCavalier();
        Permanent bear = addCreatureReady(player1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bear.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An already untapped Cavalier can still be removed from combat")
    void untappedCavalierCanRetreat() {
        Permanent cavalier = addCavalier();
        cavalier.setAttacking(true);
        cavalier.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new BenalishCavalry());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cavalier.isTapped()).isFalse();
        assertThat(cavalier.isAttacking()).isFalse();
        assertThat(cavalier.getAttackTarget()).isNull();
        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addCavalier() {
        return addCreatureReady(player1, new GustcloakCavalier());
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new AshcoatBear());
    }
}
