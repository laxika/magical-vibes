package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MageRingBully.class, GrizzlyBears.class, Shock.class})
class MageRingBullyTest extends BaseCardTest {

    private Permanent addBully() {
        return addCreatureReady(player1, new MageRingBully());
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Prowess: casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        harness.addToBattlefield(player1, new MageRingBully());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent bully = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bully)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bully)).isEqualTo(3);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, bully)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bully)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prowess: casting a creature spell does not pump")
    void creatureSpellDoesNotPump() {
        Permanent bully = addBully();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, bully)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declaring no attackers while Mage-Ring Bully can attack is illegal")
    void mustAttackWhenAble() {
        addBully();
        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Attacking with Mage-Ring Bully deals 2 damage to the defending player")
    void attacksForTwo() {
        harness.setLife(player2, 20);
        addBully();
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Mage-Ring Bully with summoning sickness does not have to attack")
    void doesNotAttackWithSummoningSickness() {
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new MageRingBully());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prowess resolves before its spell and stacks for multiple casts")
    void prowessStacksAndResolvesBeforeSpell() {
        Permanent bully = addBully();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gqs.getEffectivePower(gd, bully)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bully)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bully)).isEqualTo(3);
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, bully)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bully)).isEqualTo(4);
        harness.assertLife(player2, 16);

        endTurn();
        assertThat(gqs.getEffectivePower(gd, bully)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bully)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentsSpellDoesNotPump() {
        Permanent bully = addBully();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bully)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bully)).isEqualTo(2);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A tapped Mage-Ring Bully does not have to attack")
    void tappedBullyDoesNotHaveToAttack() {
        Permanent bully = addBully();
        bully.tap();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));

        harness.assertLife(player2, 18);
        assertThat(bully.isAttacking()).isFalse();
    }
}
