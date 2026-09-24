package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.simulation.*;
import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class CommanderSimulationTest {
    @Test void simulatorEnumeratesAndCastsCommanderWithoutAHandCard() {
        GameTestHarness harness = new GameTestHarness(); harness.skipMulligan();
        Player player = harness.getPlayer1(); GameData game = harness.getGameData();
        harness.setHand(player, List.of()); harness.forceActivePlayer(player); harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        game.priorityPassedBy.clear(); game.stack.clear(); game.format = DeckFormat.COMMANDER;
        Card commander = new Card(); commander.setName("Simulation Commander"); commander.setType(CardType.CREATURE);
        commander.setPower(2); commander.setToughness(2); commander.setManaCost("{1}"); commander.setOwnerId(player.getId()); commander.freeze();
        game.makeCommander(player.getId(), commander);
        game.playerCommandZones.put(player.getId(), new ArrayList<>(List.of(commander)));
        harness.addMana(player, ManaColor.COLORLESS, 1);
        GameSimulator simulator = HeadlessSimulationContext.getSimulator();
        SimulationAction.PlayCard cast = simulator.getLegalActions(game, player.getId()).stream()
                .filter(action -> action instanceof SimulationAction.PlayCard play && commander.getId().equals(play.commandCardId()))
                .map(SimulationAction.PlayCard.class::cast).findFirst().orElseThrow();
        simulator.applyAction(game, player.getId(), cast);
        assertThat(game.playerCommandZones.get(player.getId())).isEmpty();
        assertThat(game.commanderTaxByCardId.get(commander.getId())).isEqualTo(2);
        assertThat(game.playerHands.get(player.getId())).isEmpty();
        assertThat(game.commandCastCardId).isNull();
    }
}
