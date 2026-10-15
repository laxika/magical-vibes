package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FootSoldiers;
import com.github.laxika.magicalvibes.cards.c.CloudExSOLDIER;
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

@CardUsed({SOLDIERMilitaryProgram.class, FootSoldiers.class, CloudExSOLDIER.class})
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
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new CloudExSOLDIER());
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

    @Test
    void counterModeMayChooseNoSoldiers() {
        addProgram();
        Permanent soldier = addSoldier(player1);

        beginCombat();
        harness.handleListChoice(player1, "Put a +1/+1 counter on each of up to two Soldiers you control");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterModeWithNoSoldiersResolvesWithoutCreatingToken() {
        addProgram();

        beginCombat();
        harness.handleListChoice(player1, "Put a +1/+1 counter on each of up to two Soldiers you control");
        harness.passBothPriorities();

        assertThat(soldiers(player1)).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void bothModesCanPutCounterOnNewlyCreatedToken() {
        addProgram();
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new CloudExSOLDIER());
        commander.setCommander(true);
        gd.playerCommanders.put(player1.getId(), List.of(commander.getOriginalCard()));

        beginCombat();
        harness.handleListChoice(player1, "Create a 1/1 white Soldier creature token");
        harness.handleListChoice(player1, "Put a +1/+1 counter on each of up to two Soldiers you control");
        harness.passBothPriorities();
        Permanent token = soldiers(player1).stream()
                .filter(permanent -> !permanent.getId().equals(commander.getId()))
                .findFirst().orElseThrow();
        harness.handleMultiplePermanentsChosen(player1, List.of(token.getId()));

        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void controllingOpponentsCommanderAllowsBothModes() {
        addProgram();
        CloudExSOLDIER card = new CloudExSOLDIER();
        card.setOwnerId(player2.getId());
        gd.playerCommanders.put(player2.getId(), List.of(card));
        Permanent commander = harness.addToBattlefieldAndReturn(player1, card);
        commander.setCommander(true);

        beginCombat();
        harness.handleListChoice(player1, "Create a 1/1 white Soldier creature token");
        harness.handleListChoice(player1, "Put a +1/+1 counter on each of up to two Soldiers you control");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(commander.getId()));

        assertThat(soldiers(player1)).hasSize(2);
        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void losingCommanderAfterChoosingBothDoesNotChangeModes() {
        addProgram();
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new CloudExSOLDIER());
        commander.setCommander(true);
        gd.playerCommanders.put(player1.getId(), List.of(commander.getOriginalCard()));

        beginCombat();
        harness.handleListChoice(player1, "Create a 1/1 white Soldier creature token");
        harness.handleListChoice(player1, "Put a +1/+1 counter on each of up to two Soldiers you control");
        gd.playerBattlefields.get(player1.getId()).remove(commander);
        gd.playerGraveyards.get(player1.getId()).add(commander.getOriginalCard());
        harness.passBothPriorities();
        Permanent token = soldiers(player1).getFirst();
        harness.handleMultiplePermanentsChosen(player1, List.of(token.getId()));

        assertThat(soldiers(player1)).hasSize(1);
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        addProgram();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(soldiers(player1)).isEmpty();
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
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }
}
