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

    @Test
    @DisplayName("Creatures entering before resolution are affected, but later creatures are not")
    void affectedCreaturesAreDeterminedAtResolution() {
        addCreatureReady(player1, new BolgEreborsReckoning());
        advanceToBeginningOfCombat(player1);

        Permanent goblin = addCreatureReady(player1, new ZhurTaaGoblin());
        Permanent opponent = addCreatureReady(player2, new HillGiant());
        harness.passBothPriorities();

        Permanent lateGoblin = addCreatureReady(player1, new ZhurTaaGoblin());
        Permanent lateOpponent = addCreatureReady(player2, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, lateGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, lateOpponent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lateOpponent)).isEqualTo(3);
    }

    @Test
    @DisplayName("The combat ability still resolves after Bolg leaves the battlefield")
    void triggerResolvesWithoutItsSource() {
        Permanent bolg = addCreatureReady(player1, new BolgEreborsReckoning());
        Permanent goblin = addCreatureReady(player1, new ZhurTaaGoblin());
        Permanent opponent = addCreatureReady(player2, new HillGiant());
        advanceToBeginningOfCombat(player1);

        gd.playerBattlefields.get(player1.getId()).remove(bolg);
        gd.playerGraveyards.get(player1.getId()).add(bolg.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat modifiers expire at cleanup")
    void modifiersExpireAtEndOfTurn() {
        addCreatureReady(player1, new BolgEreborsReckoning());
        Permanent goblin = addCreatureReady(player1, new ZhurTaaGoblin());
        Permanent opponent = addCreatureReady(player2, new HillGiant());
        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(3);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
