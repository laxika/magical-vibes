package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WardscaleDragonTest extends BaseCardTest {

    private Permanent addDragon() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new WardscaleDragon());
        dragon.setSummoningSick(false);
        return dragon;
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
        Permanent planeswalker = new Permanent(new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        dragon.setAttacking(true);
        dragon.setAttackTarget(planeswalker.getId());
        prepareOpponentToCast();

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
    }
}
