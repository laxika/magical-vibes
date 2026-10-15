package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.j.JukaiPreserver;
import com.github.laxika.magicalvibes.cards.s.SevenTailMentor;
import com.github.laxika.magicalvibes.cards.u.UnstoppableOgre;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EiganjoExemplar.class, UnstoppableOgre.class, JukaiPreserver.class, SevenTailMentor.class})
class EiganjoExemplarTest extends BaseCardTest {

    @Test
    @DisplayName("A Samurai attacking alone gets +1/+1 until end of turn")
    void samuraiAttackingAloneGetsBoosted() {
        addCreatureReady(player1, new EiganjoExemplar());
        Permanent samurai = addCreatureReady(player1, new SevenTailMentor());
        int samuraiPower = gqs.getEffectivePower(gd, samurai);
        int samuraiToughness = gqs.getEffectiveToughness(gd, samurai);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(samuraiPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(samuraiToughness + 1);
    }

    @Test
    @DisplayName("A Warrior attacking alone gets +1/+1 until end of turn")
    void warriorAttackingAloneGetsBoosted() {
        addCreatureReady(player1, new EiganjoExemplar());
        Permanent warrior = addCreatureReady(player1, new UnstoppableOgre());
        int warriorPower = gqs.getEffectivePower(gd, warrior);
        int warriorToughness = gqs.getEffectiveToughness(gd, warrior);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(warriorPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(warriorToughness + 1);
    }

    @Test
    @DisplayName("A non-Samurai, non-Warrior attacking alone is not boosted")
    void otherCreatureAttackingAloneIsNotBoosted() {
        addCreatureReady(player1, new EiganjoExemplar());
        Permanent bears = addCreatureReady(player1, new JukaiPreserver());
        int bearsPower = gqs.getEffectivePower(gd, bears);
        int bearsToughness = gqs.getEffectiveToughness(gd, bears);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(bearsPower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(bearsToughness);
    }

    @Test
    @DisplayName("The trigger does not fire when multiple creatures attack")
    void multipleAttackersAreNotBoosted() {
        addCreatureReady(player1, new EiganjoExemplar());
        Permanent warrior = addCreatureReady(player1, new UnstoppableOgre());
        int warriorPower = gqs.getEffectivePower(gd, warrior);
        int warriorToughness = gqs.getEffectiveToughness(gd, warrior);
        Permanent bears = addCreatureReady(player1, new JukaiPreserver());
        int bearsPower = gqs.getEffectivePower(gd, bears);
        int bearsToughness = gqs.getEffectiveToughness(gd, bears);

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.stack).isEmpty();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(warriorPower);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(warriorToughness);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(bearsPower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(bearsToughness);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new EiganjoExemplar());
        Permanent warrior = addCreatureReady(player1, new UnstoppableOgre());
        int warriorPower = gqs.getEffectivePower(gd, warrior);
        int warriorToughness = gqs.getEffectiveToughness(gd, warrior);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(warriorPower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(warriorPower);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(warriorToughness);
    }

    @Test
    @DisplayName("Exemplar boosts itself when it attacks alone")
    void boostsItselfWhenAttackingAlone() {
        Permanent exemplar = addCreatureReady(player1, new EiganjoExemplar());
        int power = gqs.getEffectivePower(gd, exemplar);
        int toughness = gqs.getEffectiveToughness(gd, exemplar);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, exemplar)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, exemplar)).isEqualTo(toughness + 1);
    }

    @Test
    @DisplayName("An opponent's lone Samurai does not trigger Exemplar")
    void opposingSamuraiDoesNotTrigger() {
        addCreatureReady(player1, new EiganjoExemplar());
        Permanent samurai = addCreatureReady(player2, new SevenTailMentor());
        int power = gqs.getEffectivePower(gd, samurai);
        int toughness = gqs.getEffectiveToughness(gd, samurai);

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(toughness);
    }

    @Test
    @DisplayName("Multiple Exemplars independently boost a lone Samurai")
    void multipleExemplarsBoostTheSameAttacker() {
        Permanent attacker = addCreatureReady(player1, new EiganjoExemplar());
        Permanent other = addCreatureReady(player1, new EiganjoExemplar());
        int power = gqs.getEffectivePower(gd, attacker);
        int toughness = gqs.getEffectiveToughness(gd, attacker);
        int otherPower = gqs.getEffectivePower(gd, other);

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(power + 2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughness + 2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(otherPower);
    }
}
