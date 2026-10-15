package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightOfMeadowgrain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtweftImbuer.class, KnightOfMeadowgrain.class, GrizzlyBears.class})
class ThoughtweftImbuerTest extends BaseCardTest {

    @Test
    @DisplayName("Thoughtweft Imbuer can boost itself when it attacks alone")
    void boostsItselfWhenAttackingAlone() {
        Permanent imbuer = addCreatureReady(player1, new ThoughtweftImbuer());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, imbuer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, imbuer)).isEqualTo(6);
    }

    @Test
    @DisplayName("Multiple Imbuers each boost the lone attacker")
    void multipleImbuersTriggerIndependently() {
        Permanent attacker = addCreatureReady(player1, new ThoughtweftImbuer());
        addCreatureReady(player1, new ThoughtweftImbuer());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(9);
    }

    @Test
    @DisplayName("Kithkin are counted at resolution and the boost stays fixed afterward")
    void countsKithkinAtResolution() {
        Permanent attacker = addCreatureReady(player1, new ThoughtweftImbuer());

        declareAttackers(List.of(0));
        harness.addToBattlefield(player1, new ThoughtweftImbuer());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(7);

        harness.addToBattlefield(player1, new ThoughtweftImbuer());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(7);
    }

    @Test
    @DisplayName("The trigger survives its source leaving and counts only remaining Kithkin")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new ThoughtweftImbuer());
        addCreatureReady(player1, new KnightOfMeadowgrain());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(2));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost is zero when no Kithkin remain at resolution")
    void givesNoBoostWhenNoKithkinRemain() {
        Permanent source = addCreatureReady(player1, new ThoughtweftImbuer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent attacking alone does not trigger your Imbuer")
    void doesNotBoostOpponentAttacker() {
        addCreatureReady(player1, new ThoughtweftImbuer());
        Permanent attacker = addCreatureReady(player2, new ThoughtweftImbuer());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(6);
    }

    @Test
    @DisplayName("A creature attacking alone gets +X/+X for each Kithkin you control")
    void boostsAloneAttackerByKithkinCount() {
        addCreatureReady(player1, new ThoughtweftImbuer());
        addCreatureReady(player1, new KnightOfMeadowgrain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(2));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost does not count an opponent's Kithkin")
    void countsOnlyYourKithkin() {
        addCreatureReady(player1, new ThoughtweftImbuer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new KnightOfMeadowgrain());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability does not trigger when more than one creature attacks")
    void doesNotTriggerWhenAttackingWithMoreThanOneCreature() {
        addCreatureReady(player1, new ThoughtweftImbuer());
        Permanent kithkin = addCreatureReady(player1, new KnightOfMeadowgrain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kithkin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ThoughtweftImbuer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
