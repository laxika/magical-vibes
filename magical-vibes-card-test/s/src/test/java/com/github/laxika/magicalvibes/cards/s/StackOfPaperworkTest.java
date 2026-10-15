package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StackOfPaperwork.class, GrizzlyBears.class, Unsummon.class, Stifle.class, Fog.class})
class StackOfPaperworkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and draws a card")
    void entersAndDrawsCard() {
        GrizzlyBears cardToDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(cardToDraw));
        harness.castFromHand(player1, new StackOfPaperwork(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardToDraw);
    }

    @Test
    @DisplayName("Puts assigned combat damage on the stack before dealing it")
    void putsCombatDamageOnStack() {
        harness.addToBattlefield(player1, new StackOfPaperwork());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttackTarget(player2.getId());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void removedAttackerStillDealsAssignedDamage() {
        harness.addToBattlefield(player1, new StackOfPaperwork());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttackTarget(player2.getId());
        attacker.setAttacking(true);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(attacker.getCard());
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void removedBlockerDoesNotPassItsAssignedDamageToAnotherCreature() {
        harness.addToBattlefield(player1, new StackOfPaperwork());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttackTarget(player2.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.castAndResolveInstant(player2, 0, blocker.getId());
        assertThat(gd.playerHands.get(player2.getId())).contains(blocker.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bystander);
        assertThat(bystander.getMarkedDamage()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        harness.assertLife(player2, 20);
    }

    @Test
    void combatDamageCannotBeTargetedByStifle() {
        harness.addToBattlefield(player1, new StackOfPaperwork());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttackTarget(player2.getId());
        attacker.setAttacking(true);
        harness.setHand(player2, List.of(new Stifle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        UUID damageObjectId = gd.stack.getLast().getTargetableId();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, damageObjectId))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void fogRespondingToCombatDamagePreventsIt() {
        harness.addToBattlefield(player1, new StackOfPaperwork());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttackTarget(player2.getId());
        attacker.setAttacking(true);
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.castAndResolveInstant(player2, 0);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }
}
