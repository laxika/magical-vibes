package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Ichthyomorphosis;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirrorShield.class, NyxbornCourser.class, MossViper.class, Ichthyomorphosis.class})
class MirrorShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +0/+2 and hexproof")
    void equippedCreatureGetsBoostAndHexproof() {
        Permanent creature = addCreatureReady(player1, new NyxbornCourser());
        Permanent shield = addCreatureReady(player1, new MirrorShield());
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature destroys the deathtouch creature it blocks")
    void equippedCreatureBlocksDeathtouchCreature() {
        Permanent attacker = addCreatureReady(player1, new MossViper());
        attacker.setAttacking(true);

        Permanent creature = addCreatureReady(player2, new NyxbornCourser());
        Permanent shield = addCreatureReady(player2, new MirrorShield());
        shield.setAttachedTo(creature.getId());

        declareBlock(attacker, creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Moss Viper");
        harness.assertOnBattlefield(player2, "Nyxborn Courser");
    }

    @Test
    @DisplayName("Equipped creature destroys a deathtouch creature blocking it when it attacks")
    void deathtouchCreatureBlocksEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new NyxbornCourser());
        Permanent shield = addCreatureReady(player1, new MirrorShield());
        shield.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new MossViper());

        declareBlock(creature, blocker);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Moss Viper");
        harness.assertOnBattlefield(player1, "Nyxborn Courser");
    }

    @Test
    @DisplayName("A creature without deathtouch does not trigger Mirror Shield")
    void nonDeathtouchCreatureDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new NyxbornCourser());
        attacker.setAttacking(true);

        Permanent creature = addCreatureReady(player2, new NyxbornCourser());
        Permanent shield = addCreatureReady(player2, new MirrorShield());
        shield.setAttachedTo(creature.getId());

        declareBlock(attacker, creature);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipMovesTheBoostAndHexproofToTheNewCreature() {
        Permanent first = addCreatureReady(player1, new NyxbornCourser());
        Permanent second = addCreatureReady(player1, new NyxbornCourser());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new MirrorShield());
        shield.setAttachedTo(first.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, second, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void hexproofPreventsOpponentsFromTargetingTheEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new NyxbornCourser());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new MirrorShield());
        shield.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Ichthyomorphosis()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new NyxbornCourser());
        harness.addToBattlefield(player1, new MirrorShield());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent creature = addCreatureReady(player1, new NyxbornCourser());
        harness.addToBattlefield(player1, new MirrorShield());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hexproofOnTheDeathtouchBlockerDoesNotPreventDestruction() {
        Permanent attacker = addCreatureReady(player1, new NyxbornCourser());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new MirrorShield());
        shield.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MossViper());
        Permanent opposingShield = harness.addToBattlefieldAndReturn(player2, new MirrorShield());
        opposingShield.setAttachedTo(blocker.getId());

        declareBlock(attacker, blocker);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Moss Viper");
        harness.assertOnBattlefield(player1, "Nyxborn Courser");
    }

    @Test
    void eachDeathtouchBlockerTriggersSeparately() {
        Permanent attacker = addCreatureReady(player1, new NyxbornCourser());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new MirrorShield());
        shield.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MossViper());
        addCreatureReady(player2, new MossViper());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Moss Viper")).hasSize(2);
        harness.assertOnBattlefield(player1, "Nyxborn Courser");
    }

    @Test
    void removingShieldAfterTriggeringDoesNotStopDestruction() {
        Permanent attacker = addCreatureReady(player1, new NyxbornCourser());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new MirrorShield());
        shield.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MossViper());

        declareBlock(attacker, blocker);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(shield);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Moss Viper");
        harness.assertOnBattlefield(player1, "Nyxborn Courser");
    }

    @Test
    void blockingCreatureControlsTheGrantedTriggerEvenWhenOpponentControlsShield() {
        Permanent attacker = addCreatureReady(player1, new MossViper());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new NyxbornCourser());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new MirrorShield());
        shield.setAttachedTo(blocker.getId());

        declareBlock(attacker, blocker);

        assertThat(gd.stack).singleElement().satisfies(trigger -> {
            assertThat(trigger.getControllerId()).isEqualTo(player2.getId());
            assertThat(trigger.getSourcePermanentId()).isEqualTo(blocker.getId());
        });
    }

    @Test
    void attackingCreatureIsTheSourceOfTheGrantedTrigger() {
        Permanent attacker = addCreatureReady(player1, new NyxbornCourser());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new MirrorShield());
        shield.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MossViper());

        declareBlock(attacker, blocker);

        assertThat(gd.stack).singleElement().satisfies(trigger ->
                assertThat(trigger.getSourcePermanentId()).isEqualTo(attacker.getId()));
    }

    @Test
    void laterAbilityRemovalRemovesTheGrantedCombatTrigger() {
        Permanent attacker = addCreatureReady(player1, new NyxbornCourser());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new MirrorShield());
        shield.setAttachedTo(attacker.getId());
        shield.setTimestamp(gd.nextTimestamp());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Ichthyomorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MossViper());

        declareBlock(attacker, blocker);

        assertThat(gd.stack).isEmpty();
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
