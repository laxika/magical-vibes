package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoraugFuryOfAkoum.class, GrizzlyBears.class, Mountain.class})
class MoraugFuryOfAkoumTest extends BaseCardTest {

    @Test
    void boostsEachCreatureByItsNumberOfAttacksThisTurn() {
        Permanent moraug = addCreatureReady(player1, new MoraugFuryOfAkoum());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, moraug)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);

        declareAttackers(List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, moraug)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);

        resolveCombat();
        moraug.untap();
        bear.untap();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, moraug)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
    }

    @Test
    void landfallDuringMainPhaseAddsCombatAndUntapsControlledCreatures() {
        addCreatureReady(player1, new MoraugFuryOfAkoum());
        Permanent tappedBear = addCreatureReady(player1, new GrizzlyBears());
        tappedBear.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(tappedBear.isTapped()).isFalse();
    }
}
