package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptainsManeuver.class, GrizzlyBears.class, ProdigalPyromancer.class})
class CaptainsManeuverTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects the next X damage from a creature to another creature")
    void redirectsDamageBetweenCreatures() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        castManeuver(2, protectedCreature.getId(), destination.getId());

        activatePing(pyromancer, protectedCreature.getId());

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Redirects damage from one player to another player")
    void redirectsDamageBetweenPlayers() {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castManeuver(1, player1.getId(), player2.getId());

        activatePing(pyromancer, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Redirects only X damage and requires different targets")
    void limitsRedirectedDamageAndRejectsSharedTarget() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        Permanent secondPyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        castManeuver(1, protectedCreature.getId(), destination.getId());

        activatePing(pyromancer, protectedCreature.getId());
        activatePing(secondPyromancer, protectedCreature.getId());

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);

        harness.setHand(player1, List.of(new CaptainsManeuver()));
        addMana(1);
        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1,
                        List.of(protectedCreature.getId(), protectedCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castManeuver(int xValue, java.util.UUID protectedId, java.util.UUID destinationId) {
        harness.setHand(player1, List.of(new CaptainsManeuver()));
        addMana(xValue);
        harness.castInstantForX(player1, 0, xValue, List.of(protectedId, destinationId));
        harness.passBothPriorities();
    }

    private void activatePing(Permanent pyromancer, java.util.UUID targetId) {
        pyromancer.setSummoningSick(false);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer), null, targetId);
        harness.passBothPriorities();
    }

    private void addMana(int xValue) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
