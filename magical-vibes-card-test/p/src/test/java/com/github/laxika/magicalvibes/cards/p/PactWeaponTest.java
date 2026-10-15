package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlhammarretsArchive;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PactWeapon.class, GrizzlyBears.class, Forest.class, AlhammarretsArchive.class})
class PactWeaponTest extends BaseCardTest {

    @Test
    @DisplayName("Attached Pact Weapon prevents losing for having zero or less life")
    void attachedWeaponPreventsLifeTotalLoss() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent weapon = harness.addToBattlefieldAndReturn(player1, new PactWeapon());
        weapon.setAttachedTo(creature.getId());

        assertThat(gqs.canPlayerLoseFromLife(gd, player1.getId())).isFalse();

        weapon.setAttachedTo(null);
        assertThat(gqs.canPlayerLoseFromLife(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Attacking with Pact Weapon draws, reveals, pumps the creature, and loses life")
    void attackTriggerUsesDrawnCardManaValue() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent weapon = harness.addToBattlefieldAndReturn(player1, new PactWeapon());
        weapon.setAttachedTo(creature.getId());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("A zero-mana-value drawn card produces no pump or life loss")
    void zeroManaValueDrawDoesNothing() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent weapon = harness.addToBattlefieldAndReturn(player1, new PactWeapon());
        weapon.setAttachedTo(creature.getId());
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unattached Pact Weapon does not trigger when a creature attacks")
    void unattachedWeaponDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new PactWeapon());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip discards a card and attaches Pact Weapon")
    void equipDiscardsCardAndAttaches() {
        Permanent weapon = harness.addToBattlefieldAndReturn(player1, new PactWeapon());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Card discarded = new Forest();
        harness.setHand(player1, List.of(discarded));

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(weapon.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("Replacing the draw with two draws does not reveal, boost, or lose life")
    void replacedDrawDoesNotApplyManaValueEffects() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent weapon = harness.addToBattlefieldAndReturn(player1, new PactWeapon());
        weapon.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new AlhammarretsArchive());
        Card first = new GrizzlyBears();
        Card second = new PactWeapon();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gameLogContains(" reveals ")).isFalse();
    }

    @Test
    @DisplayName("The Equipment controller draws and loses life when an opponent attacks with its creature")
    void opponentControlledCreatureStillTriggersForWeaponController() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent weapon = harness.addToBattlefieldAndReturn(player1, new PactWeapon());
        weapon.setAttachedTo(creature.getId());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawn));

        assertThat(gqs.canPlayerLoseFromLife(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerLoseFromLife(gd, player2.getId())).isTrue();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gameLogContains(" reveals Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("Moving the Equipment before resolution still boosts the original attacker")
    void movingWeaponDoesNotChangeTriggeredCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent weapon = harness.addToBattlefieldAndReturn(player1, new PactWeapon());
        weapon.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        weapon.setAttachedTo(otherCreature.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger can reduce life below zero while the Equipment remains attached")
    void attackTriggerCanLoseLifeBelowZero() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent weapon = harness.addToBattlefieldAndReturn(player1, new PactWeapon());
        weapon.setAttachedTo(creature.getId());
        harness.setLife(player1, 1);
        harness.setLibrary(player1, List.of(new PactWeapon()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(-3);
        assertThat(gqs.canPlayerLoseFromLife(gd, player1.getId())).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("The attack bonus expires at the end of the turn")
    void attackBonusExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent weapon = harness.addToBattlefieldAndReturn(player1, new PactWeapon());
        weapon.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }
}
