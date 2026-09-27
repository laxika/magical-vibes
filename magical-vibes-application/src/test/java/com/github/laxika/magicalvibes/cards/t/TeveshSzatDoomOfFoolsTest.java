package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeveshSzatDoomOfFools.class, GrizzlyBears.class})
class TeveshSzatDoomOfFoolsTest extends BaseCardTest {

    @Test
    @DisplayName("+2 creates two Thrulls")
    void plusTwoCreatesThrulls() {
        Permanent tevesh = addReadyTevesh(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(tevesh.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(findPermanents(player1, "Thrull")).hasSize(2);
    }

    @Test
    @DisplayName("+1 can sacrifice a commander and draw three cards")
    void plusOneSacrificesCommanderAndDrawsThree() {
        Permanent tevesh = addReadyTevesh(player1);
        Permanent commander = addCreatureReady(player1, new GrizzlyBears());
        commander.setCommander(true);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, commander.getId());
        harness.passBothPriorities();

        assertThat(tevesh.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(commander);
    }

    @Test
    @DisplayName("-10 takes battlefield commanders and puts command-zone commanders onto the battlefield")
    void minusTenTakesAndReturnsCommanders() {
        Permanent tevesh = addReadyTevesh(player1);
        tevesh.setCounterCount(CounterType.LOYALTY, 10);
        Permanent battlefieldCommander = addCreatureReady(player2, new GrizzlyBears());
        battlefieldCommander.setCommander(true);
        Card commandZoneCommander = new GrizzlyBears();
        gd.playerCommandZones.get(player2.getId()).add(commandZoneCommander);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerCommandZones.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(battlefieldCommander)
                .anyMatch(permanent -> permanent.getCard().getId().equals(commandZoneCommander.getId())
                        && permanent.isCommander());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(battlefieldCommander);
    }

    private Permanent addReadyTevesh(Player player) {
        Permanent tevesh = new Permanent(new TeveshSzatDoomOfFools());
        tevesh.setCounterCount(CounterType.LOYALTY, 4);
        tevesh.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(tevesh);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return tevesh;
    }
}
