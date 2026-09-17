package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.OrcishVeteran;
import com.github.laxika.magicalvibes.cards.z.ZhurTaaGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BolgEreborsReckoning.class, GrizzlyBears.class, HillGiant.class,
        OrcishVeteran.class, ZhurTaaGoblin.class})
class BolgEreborsReckoningTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of each combat boosts other Goblins and Orcs and shrinks opposing creatures")
    void combatTriggerAppliesBothEffects() {
        Permanent bolg = addCreatureReady(player1, new BolgEreborsReckoning());
        Permanent goblin = addCreatureReady(player1, new ZhurTaaGoblin());
        Permanent orc = addCreatureReady(player1, new OrcishVeteran());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new HillGiant());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bolg)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bolg)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, orc)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, orc)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability triggers during an opponent's combat as well")
    void combatTriggerFiresOnOpponentsTurn() {
        addCreatureReady(player1, new BolgEreborsReckoning());
        Permanent goblin = addCreatureReady(player1, new ZhurTaaGoblin());
        Permanent opponent = addCreatureReady(player2, new HillGiant());

        advanceToBeginningOfCombat(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
