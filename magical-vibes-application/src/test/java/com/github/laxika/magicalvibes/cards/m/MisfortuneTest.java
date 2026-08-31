package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshnodsCylix;
import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Misfortune.class, ElvishRanger.class, AshnodsCylix.class})
class MisfortuneTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving prompts the opponent to choose a mode")
    void resolvingPromptsOpponentChoice() {
        setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Accept: +1/+1 counter on each creature the controller controls and they gain 4 life")
    void acceptGrowsControllerCreaturesAndGainsLife() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new ElvishRanger());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        setupAndCast();
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(mine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(theirs.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Decline: -1/-1 counter on each of that player's creatures and 4 damage to them")
    void declineShrinksOpponentCreaturesAndBurnsThem() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new ElvishRanger());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        setupAndCast();
        GameData gd = harness.getGameData();
        int controllerLife = gd.playerLifeTotals.get(player1.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(theirs.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(mine.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife);
    }

    @Test
    void acceptIgnoresNoncreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishRanger());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AshnodsCylix());
        setupAndCast();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void declineIgnoresNoncreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AshnodsCylix());
        setupAndCast();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new Misfortune(), "{1}{B}{R}{G}");
    }
}
