package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CourierBat;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OliviasAttendants.class, CourierBat.class})
class OliviasAttendantsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Blood token for each combat damage dealt to a player")
    void combatDamageCreatesBloodTokens() {
        Permanent attendants = addReadyAttendants(3);
        attendants.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(findPermanents(player1, "Blood")).hasSize(3)
                .allSatisfy(blood -> {
                    assertThat(blood.getCard().isToken()).isTrue();
                    assertThat(blood.getCard().getType()).isEqualTo(CardType.ARTIFACT);
                    assertThat(blood.getCard().getSubtypes()).contains(CardSubtype.BLOOD);
                });
    }

    @Test
    @DisplayName("The activated ability deals damage to a creature and creates a Blood token")
    void activatedAbilityDamagesCreatureAndCreatesBlood() {
        addReadyAttendants(6);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CourierBat());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    @DisplayName("The ability can damage a player while its source is tapped and summoning sick")
    void activatedAbilityDamagesPlayerWhileTappedAndSummoningSick() {
        Permanent attendants = harness.addToBattlefieldAndReturn(player1, new OliviasAttendants());
        attendants.setSummoningSick(true);
        attendants.setTapped(true);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(findPermanents(player2, "Blood")).isEmpty();
    }

    @Test
    @DisplayName("Lethal self-damage still creates Blood after the source dies")
    void lethalSelfDamageStillCreatesBlood() {
        Permanent attendants = addReadyAttendants(6);
        attendants.setMarkedDamage(5);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, attendants.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Olivia's Attendants");
        harness.assertNotOnBattlefield(player1, "Olivia's Attendants");
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    @DisplayName("An ability with a target that left the battlefield creates no Blood")
    void missingTargetCreatesNoBlood() {
        addReadyAttendants(6);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CourierBat());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Blood")).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage from an ability whose source already left creates no Blood")
    void sourceLeavingBeforeResolutionCreatesNoBlood() {
        Permanent attendants = addReadyAttendants(6);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(attendants);
        gd.playerGraveyards.get(player1.getId()).add(attendants.getCard());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    @Test
    @DisplayName("Zero combat damage creates no Blood")
    void zeroCombatDamageCreatesNoBlood() {
        Permanent attendants = addReadyAttendants(0);
        attendants.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    private Permanent addReadyAttendants(int power) {
        OliviasAttendants card = new OliviasAttendants();
        card.setPower(power);
        card.setToughness(6);
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
