package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.SilvercoatLion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LightOfTheLegionTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor puts a +1/+1 counter on a lesser-power attacking creature")
    void mentorCountersLesserPowerAttacker() {
        Permanent light = addCreatureReady(player1, new LightOfTheLegion());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(light, attacker);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When Light of the Legion dies, each white creature you control gets a counter")
    void deathCountersWhiteCreaturesOnly() {
        Permanent light = addCreatureReady(player1, new LightOfTheLegion());
        Permanent whiteCreature = addCreatureReady(player1, new SilvercoatLion());
        Permanent greenCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, light.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(whiteCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(greenCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void declareAttackers(Permanent first, Permanent second) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));
    }
}
