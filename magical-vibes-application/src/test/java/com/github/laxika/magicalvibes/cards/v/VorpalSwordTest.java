package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VorpalSword.class, DireWolfProwler.class})
class VorpalSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0 and deathtouch")
    void equippedCreatureGetsBonuses() {
        Permanent creature = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Activated ability makes the damaged player lose the game")
    void activatedAbilityMakesDamagedPlayerLose() {
        Permanent creature = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The granted ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        creature.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Vorpal Sword can be cast for one black mana")
    void castsForOneBlackMana() {
        harness.setHand(player1, List.of(new VorpalSword()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vorpal Sword");
    }

    @Test
    @DisplayName("Equip costs two black mana and moves the bonuses to the new creature")
    void equipMovesBonuses() {
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 2, 1, null, second.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Combat damage without activation does not make the player lose")
    void combatDamageWithoutActivationDoesNotCauseLoss() {
        Permanent creature = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The Sword can gain its ability while unattached and equip afterward")
    void canActivateBeforeEquipping() {
        Permanent creature = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 1, 1, null, creature.getId());
        harness.passBothPriorities();
        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        creature.setAttacking(true);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The granted ability follows the Sword rather than its former equipped creature")
    void formerEquippedCreatureDoesNotCauseLoss() {
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 2, 1, null, second.getId());
        harness.passBothPriorities();
        first.setAttacking(true);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("A triggered loss still resolves after the Sword leaves the battlefield")
    void triggeredLossSurvivesSwordLeaving() {
        Permanent creature = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        creature.setAttacking(true);

        harness.resolveCombatDamage();
        harness.assertLife(player2, 16);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(sword);
        gd.playerGraveyards.get(player1.getId()).add(sword.getCard());
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The Sword's controller can lose to combat damage from an opponent's equipped creature")
    void damagesSwordControllerAndMakesThemLose() {
        Permanent creature = addCreatureReady(player2);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        creature.setAttacking(true);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    private Permanent addSwordReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new VorpalSword());
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new DireWolfProwler());
    }
}
