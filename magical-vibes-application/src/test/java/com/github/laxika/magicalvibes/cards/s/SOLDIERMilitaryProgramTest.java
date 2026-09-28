package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FootSoldiers;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SOLDIERMilitaryProgram.class, FootSoldiers.class})
class SOLDIERMilitaryProgramTest extends BaseCardTest {

    @Test
    void withoutCommanderChoosesOneModeAndCreatesSoldierToken() {
        addProgram();

        beginCombat();
        harness.handleListChoice(player1, "Create a 1/1 white Soldier creature token");
        harness.passBothPriorities();

        assertThat(soldiers(player1)).hasSize(1);
    }

    @Test
    void withoutCommanderCounterModePutsCountersOnUpToTwoSoldiers() {
        addProgram();
        Permanent soldier = addSoldier(player1);

        beginCombat();
        harness.handleListChoice(player1, "Put a +1/+1 counter on each of up to two Soldiers you control");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(soldier.getId()));
        harness.passBothPriorities();

        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void withCommanderMayChooseBothModes() {
        addProgram();
        Permanent commander = addSoldier(player1);
        commander.setCommander(true);
        gd.playerCommanders.put(player1.getId(), List.of(commander.getOriginalCard()));
        Permanent secondSoldier = addSoldier(player1);

        beginCombat();
        harness.handleListChoice(player1, "Create a 1/1 white Soldier creature token");
        harness.handleListChoice(player1, "Put a +1/+1 counter on each of up to two Soldiers you control");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(commander.getId(), secondSoldier.getId()));
        harness.passBothPriorities();

        assertThat(soldiers(player1)).hasSize(3);
        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondSoldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void addProgram() {
        harness.addToBattlefieldAndReturn(player1, new SOLDIERMilitaryProgram());
    }

    private Permanent addSoldier(Player player) {
        return harness.addToBattlefieldAndReturn(player, new FootSoldiers());
    }

    private List<Permanent> soldiers(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .toList();
    }

    private void beginCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
