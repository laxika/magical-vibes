package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BloodflowConnoisseurTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on Bloodflow Connoisseur")
    void sacrificeAnotherCreaturePutsCounter() {
        addConnoisseurReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        sacrificeBears();

        harness.assertInGraveyard(player1, "Grizzly Bears");

        Permanent connoisseur = findPermanent(player1, "Bloodflow Connoisseur");
        assertThat(connoisseur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(connoisseur.getEffectivePower()).isEqualTo(2);
        assertThat(connoisseur.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The +1/+1 counter stays after end of turn")
    void counterPersistsPastEndOfTurn() {
        addConnoisseurReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        sacrificeBears();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent connoisseur = findPermanent(player1, "Bloodflow Connoisseur");
        assertThat(connoisseur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters accumulate across multiple activations")
    void countersAccumulate() {
        addConnoisseurReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        sacrificeBears();
        sacrificeBears();

        Permanent connoisseur = findPermanent(player1, "Bloodflow Connoisseur");
        assertThat(connoisseur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bloodflow Connoisseur can sacrifice itself to its own ability")
    void canSacrificeItself() {
        addConnoisseurReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bloodflow Connoisseur");
        harness.assertNotOnBattlefield(player1, "Bloodflow Connoisseur");
    }

    @Test
    @DisplayName("Ability requires no mana and no tap")
    void abilityCostsNoManaAndDoesNotTap() {
        Permanent connoisseur = addConnoisseurReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        sacrificeBears();

        assertThat(connoisseur.isTapped()).isFalse();
    }

    private void sacrificeBears() {
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
    }

    private Permanent addConnoisseurReady(Player player) {
        Permanent perm = new Permanent(new BloodflowConnoisseur());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
