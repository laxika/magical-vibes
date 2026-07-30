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

import static org.assertj.core.api.Assertions.assertThat;

class ExquisiteBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains life equal to spell damage dealt to an opponent")
    void gainsLifeOnSpellDamage() {
        harness.addToBattlefield(player1, new ExquisiteBlood());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Shock (life loss triggers Exquisite Blood)
        harness.passBothPriorities(); // resolve Exquisite Blood's triggered ability

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Controller gains life equal to combat damage dealt to an opponent")
    void gainsLifeOnCombatDamage() {
        harness.addToBattlefield(player1, new ExquisiteBlood());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage (life loss triggers Exquisite Blood)
        harness.passBothPriorities(); // resolve Exquisite Blood's triggered ability

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not trigger when its controller loses life")
    void doesNotTriggerOnControllerLifeLoss() {
        harness.addToBattlefield(player1, new ExquisiteBlood());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Two copies each trigger, gaining life twice")
    void twoCopiesEachTrigger() {
        harness.addToBattlefield(player1, new ExquisiteBlood());
        harness.addToBattlefield(player1, new ExquisiteBlood());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Shock
        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Opponent's copy gains them life when we lose life")
    void opponentsCopyTriggersOnOurLifeLoss() {
        harness.addToBattlefield(player2, new ExquisiteBlood());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities(); // resolve Shock
        harness.passBothPriorities(); // resolve player2's triggered ability

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }
}
