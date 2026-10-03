package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TidehollowStrix;
import com.github.laxika.magicalvibes.cards.v.VolcanicGeyser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmpyrialArchangel.class, Shock.class, VolcanicGeyser.class, TidehollowStrix.class})
class EmpyrialArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("Damage that would be dealt to the controller is redirected to the Archangel")
    void redirectsDamageFromControllerToItself() {
        harness.addToBattlefield(player2, new EmpyrialArchangel());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Controller takes no damage; the 5/8 Archangel absorbs 2 and survives.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Empyrial Archangel");
    }

    @Test
    @DisplayName("Redirected damage meeting the Archangel's toughness destroys it, sparing the controller")
    void lethalRedirectedDamageDestroysArchangel() {
        harness.addToBattlefield(player2, new EmpyrialArchangel());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VolcanicGeyser()));
        harness.addMana(player1, ManaColor.RED, 12);

        // 8 damage aimed at the controller is redirected to the 5/8 Archangel, destroying it.
        harness.castInstant(player1, 0, 8, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player2, "Empyrial Archangel");
        harness.assertInGraveyard(player2, "Empyrial Archangel");
    }

    @Test
    void redirectedCombatDamageRetainsDeathtouch() {
        harness.addToBattlefield(player2, new EmpyrialArchangel());
        Permanent attacker = addCreatureReady(player1, new TidehollowStrix());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Empyrial Archangel");
        harness.assertNotOnBattlefield(player2, "Empyrial Archangel");
    }

    @Test
    void tappedArchangelStillRedirectsDamage() {
        Permanent archangel = harness.addToBattlefieldAndReturn(player2, new EmpyrialArchangel());
        archangel.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(archangel.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void archangelWithoutAbilitiesDoesNotRedirectDamage() {
        Permanent archangel = harness.addToBattlefieldAndReturn(player2, new EmpyrialArchangel());
        archangel.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(archangel.getMarkedDamage()).isZero();
    }

    @Test
    void controllerChoosesBetweenTwoArchangelsBeforeDamageIsDealt() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new EmpyrialArchangel());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new EmpyrialArchangel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
        harness.handlePermanentChosen(player2, second.getId());

        harness.assertLife(player2, 20);
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isEqualTo(2);
    }
}
