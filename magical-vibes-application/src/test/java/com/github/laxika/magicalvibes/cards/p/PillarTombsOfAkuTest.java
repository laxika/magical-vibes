package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PillarTombsOfAkuTest extends BaseCardTest {

    @Test
    @DisplayName("Controller declines: loses 5 life and Pillar Tombs is sacrificed")
    void controllerDeclinesLosesLifeAndSacrifices() {
        harness.addToBattlefield(player1, new PillarTombsOfAku());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Pillar Tombs of Aku");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Controller accepts with one creature: sacrifices it, Pillar Tombs survives")
    void controllerAcceptsSacrificesCreature() {
        harness.addToBattlefield(player1, new PillarTombsOfAku());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Pillar Tombs of Aku");
    }

    @Test
    @DisplayName("Controller accepts with multiple creatures: chooses which to sacrifice")
    void controllerAcceptsChoosesAmongCreatures() {
        harness.addToBattlefield(player1, new PillarTombsOfAku());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID chosen = harness.getPermanentId(player1, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, chosen);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Pillar Tombs of Aku");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("No creatures: penalty applies with no prompt")
    void noCreaturesAppliesPenaltyImmediately() {
        harness.addToBattlefield(player1, new PillarTombsOfAku());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertNotOnBattlefield(player1, "Pillar Tombs of Aku");
    }

    @Test
    @DisplayName("Opponent declines on their upkeep: opponent loses 5, controller sacrifices Pillar Tombs")
    void opponentDeclinesOpponentLosesLifeControllerSacrifices() {
        harness.addToBattlefield(player1, new PillarTombsOfAku());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Pillar Tombs of Aku");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent accepts on their upkeep: opponent sacrifices a creature, Pillar Tombs survives")
    void opponentAcceptsSacrificesTheirCreature() {
        harness.addToBattlefield(player1, new PillarTombsOfAku());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .noneMatch(p -> p.getId().equals(bears.getId()))).isTrue();
        harness.assertOnBattlefield(player1, "Pillar Tombs of Aku");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }
}
