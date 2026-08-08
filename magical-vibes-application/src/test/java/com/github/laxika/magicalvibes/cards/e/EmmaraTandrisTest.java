package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmmaraTandrisTest extends BaseCardTest {

    private Permanent addTokenCreature(UUID controllerId) {
        GrizzlyBears card = new GrizzlyBears();
        card.setToken(true);
        Permanent token = new Permanent(card);
        token.setSummoningSick(false);
        gd.playerBattlefields.get(controllerId).add(token);
        return token;
    }

    @Test
    @DisplayName("Noncombat damage to a creature token you control is prevented")
    void preventsNoncombatDamageToYourToken() {
        harness.addToBattlefield(player1, new EmmaraTandris());
        Permanent token = addTokenCreature(player1.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, token.getId());
        harness.passBothPriorities();

        assertThat(token.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage to a nontoken creature you control is not prevented")
    void doesNotPreventDamageToNontokenCreature() {
        harness.addToBattlefield(player1, new EmmaraTandris());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage to an opponent's creature token is not prevented")
    void doesNotPreventDamageToOpponentToken() {
        harness.addToBattlefield(player1, new EmmaraTandris());
        Permanent enemyToken = addTokenCreature(player2.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, enemyToken.getId());
        harness.passBothPriorities();

        assertThat(enemyToken.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to a creature token you control is prevented")
    void preventsCombatDamageToYourToken() {
        harness.addToBattlefield(player1, new EmmaraTandris());
        Permanent blocker = addTokenCreature(player1.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        GrizzlyBears attackerCard = new GrizzlyBears();
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
    }
}
