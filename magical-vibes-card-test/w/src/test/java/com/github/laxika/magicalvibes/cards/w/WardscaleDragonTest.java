package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WardscaleDragon.class, JaceBeleren.class, Shock.class, TurnToFrog.class})
class WardscaleDragonTest extends BaseCardTest {

    private Permanent addDragon() {
        return addCreatureReady(player1, new WardscaleDragon());
    }

    private void prepareOpponentToCast() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Defending player can't cast spells while Wardscale Dragon is attacking")
    void defendingPlayerCantCastWhileAttacking() {
        Permanent dragon = addDragon();
        dragon.setAttacking(true);
        dragon.setAttackTarget(player2.getId());
        prepareOpponentToCast();

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Defending player can cast spells when Wardscale Dragon is not attacking")
    void defendingPlayerCanCastWhenNotAttacking() {
        Permanent dragon = addDragon();
        dragon.setAttackTarget(player2.getId());
        prepareOpponentToCast();

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    @Test
    @DisplayName("Wardscale Dragon's controller can cast spells while it attacks")
    void controllerCanCastWhileAttacking() {
        Permanent dragon = addDragon();
        dragon.setAttacking(true);
        dragon.setAttackTarget(player2.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Attacking a planeswalker restricts its controller")
    void attackingPlaneswalkerRestrictsItsController() {
        Permanent dragon = addDragon();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        dragon.setAttacking(true);
        dragon.setAttackTarget(planeswalker.getId());
        prepareOpponentToCast();

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Losing all abilities releases the defending player from the casting restriction")
    void losingAbilitiesAllowsDefenderToCast() {
        Permanent dragon = addDragon();
        dragon.setAttacking(true);
        dragon.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castAndResolveInstant(player1, 0, dragon.getId());
        assertThat(dragon.isAttacking()).isTrue();
        prepareOpponentToCast();

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player2.getId())).contains(0);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A face-down Wardscale Dragon has no casting restriction")
    void faceDownDragonDoesNotRestrictCasting() {
        Permanent dragon = addDragon();
        dragon.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        dragon.setAttacking(true);
        dragon.setAttackTarget(player2.getId());
        prepareOpponentToCast();

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player2.getId())).contains(0);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The casting restriction ends when Wardscale Dragon stops attacking")
    void restrictionEndsWhenDragonStopsAttacking() {
        Permanent dragon = addDragon();
        dragon.setAttacking(true);
        dragon.setAttackTarget(player2.getId());
        prepareOpponentToCast();
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player2.getId())).isEmpty();

        dragon.setAttacking(false);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player2.getId())).contains(0);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Casting checks do not crash after the attacked planeswalker dies")
    void attackedPlaneswalkerLeavingDoesNotCrashCastingChecks() {
        Permanent dragon = addDragon();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);
        dragon.setAttacking(true);
        dragon.setAttackTarget(planeswalker.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());
        harness.assertNotOnBattlefield(player2, "Jace Beleren");
        assertThat(dragon.isAttacking()).isTrue();
        prepareOpponentToCast();

        assertThatCode(() -> harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player2.getId())).doesNotThrowAnyException();
    }
}
