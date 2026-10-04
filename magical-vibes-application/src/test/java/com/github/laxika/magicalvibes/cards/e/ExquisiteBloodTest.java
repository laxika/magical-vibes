package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Griselbrand;
import com.github.laxika.magicalvibes.cards.n.NaturalEnd;
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

@CardUsed({ExquisiteBlood.class, GrizzlyBears.class, Shock.class, Griselbrand.class, NaturalEnd.class})
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

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

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

    @Test
    @DisplayName("Life paid as a cost triggers life gain before the activated ability resolves")
    void gainsLifeFromOpponentPayingLife() {
        harness.addToBattlefield(player2, new ExquisiteBlood());
        harness.addToBattlefield(player1, new Griselbrand());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        harness.assertLife(player2, 27);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Separate pending life-loss triggers retain their own amounts")
    void pendingTriggersKeepSeparateAmounts() {
        harness.addToBattlefield(player2, new ExquisiteBlood());
        harness.addToBattlefield(player1, new Griselbrand());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 22);

        harness.passBothPriorities();
        harness.assertLife(player2, 29);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A queued life-gain trigger resolves after Exquisite Blood is destroyed")
    void triggerSurvivesSourceRemoval() {
        Permanent blood = harness.addToBattlefieldAndReturn(player1, new ExquisiteBlood());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new NaturalEnd()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);

        harness.castInstant(player1, 0, blood.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Exquisite Blood");
        harness.assertLife(player1, 23);

        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Damage to an opponent's creature does not count as opponent life loss")
    void doesNotTriggerOnCreatureDamage() {
        harness.addToBattlefield(player1, new ExquisiteBlood());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }
}
