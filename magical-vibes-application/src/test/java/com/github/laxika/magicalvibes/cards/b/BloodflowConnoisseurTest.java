package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodflowConnoisseur.class, GrizzlyBears.class})
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
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BloodflowConnoisseur());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution, and the counter is added on resolution")
    void sacrificeIsPaidBeforeCounterIsAdded() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BloodflowConnoisseur());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new BloodflowConnoisseur());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
        harness.assertInGraveyard(player1, "Bloodflow Connoisseur");
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Connoisseur can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BloodflowConnoisseur());
        source.setTapped(true);
        source.setSummoningSick(true);
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new BloodflowConnoisseur());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Self-sacrifice does not put a counter on another Connoisseur")
    void selfSacrificeDoesNotAffectAnotherConnoisseur() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BloodflowConnoisseur());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BloodflowConnoisseur());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source).contains(other);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Bloodflow Connoisseur");
    }
}
