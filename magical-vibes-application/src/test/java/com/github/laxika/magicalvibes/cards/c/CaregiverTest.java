package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.cards.l.LightningHelix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Caregiver.class, Watchwolf.class, LightningHelix.class})
class CaregiverTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature adds a prevention shield to a target creature")
    void sacrificesCreatureToPreventDamageToCreature() {
        addCreatureReady(player1, new Caregiver());
        harness.addToBattlefield(player1, new Watchwolf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Watchwolf());
        UUID sacrificedId = harness.getPermanentId(player1, "Watchwolf");
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, sacrificedId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Watchwolf");
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Caregiver's ability can be activated while it is tapped")
    void canActivateWhileTapped() {
        Permanent caregiver = addCreatureReady(player1, new Caregiver());
        caregiver.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Caregiver");
        harness.assertInGraveyard(player1, "Caregiver");
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("The prevention shield protects a target player from the next damage")
    void preventsNextDamageToTargetPlayer() {
        addCreatureReady(player1, new Caregiver());
        harness.addToBattlefield(player1, new Watchwolf());
        UUID sacrificedId = harness.getPermanentId(player1, "Watchwolf");
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, sacrificedId);
        harness.passBothPriorities();

        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new Watchwolf());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Caregiver can sacrifice itself to pay its ability cost")
    void canSacrificeItself() {
        addCreatureReady(player1, new Caregiver());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Caregiver");
        harness.assertInGraveyard(player1, "Caregiver");
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void preventsOnlyOneDamageAcrossSuccessiveSpells() {
        harness.addToBattlefield(player1, new Caregiver());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningHelix(), new LightningHelix()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 15);
    }

    @Test
    void preventsLethalNoncombatDamageToCreature() {
        harness.addToBattlefield(player1, new Caregiver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Watchwolf());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningHelix()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Watchwolf");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    void canTargetTheCreatureSacrificedAsItsCost() {
        harness.addToBattlefield(player1, new Caregiver());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.assertInGraveyard(player1, "Watchwolf");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getDamagePreventionShield()).isZero();
        harness.assertOnBattlefield(player1, "Caregiver");
    }
}
