package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HowlersHeavy;
import com.github.laxika.magicalvibes.cards.s.StartingColumn;
import com.github.laxika.magicalvibes.cards.d.DuneDrifter;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GastalBlockbuster.class, HowlersHeavy.class, StartingColumn.class, DuneDrifter.class})
class GastalBlockbusterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature destroys an artifact an opponent controls")
    void sacrificeCreatureDestroysOpponentsArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HowlersHeavy());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new StartingColumn());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new StartingColumn());
        castGastalBlockbuster();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice sacrificeChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(sacrificeChoice.validIds()).contains(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());

        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.validIds()).containsExactly(opponentArtifact.getId());
        harness.handlePermanentChosen(player1, opponentArtifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Howler's Heavy");
        harness.assertOnBattlefield(player1, "Starting Column");
        harness.assertNotOnBattlefield(player2, "Starting Column");
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(ownArtifact.getId());
    }

    @Test
    @DisplayName("A Vehicle can be sacrificed for the ability")
    void sacrificeVehicleDestroysOpponentsArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DuneDrifter());
        harness.addToBattlefield(player2, new StartingColumn());
        castGastalBlockbuster();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice sacrificeChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(sacrificeChoice.validIds()).contains(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Starting Column"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dune Drifter");
        harness.assertNotOnBattlefield(player2, "Starting Column");
    }

    @Test
    @DisplayName("Declining the sacrifice does nothing")
    void decliningSacrificeDoesNothing() {
        harness.addToBattlefield(player1, new HowlersHeavy());
        harness.addToBattlefield(player2, new StartingColumn());
        castGastalBlockbuster();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Howler's Heavy");
        harness.assertOnBattlefield(player1, "Gastal Blockbuster");
        harness.assertOnBattlefield(player2, "Starting Column");
    }

    @Test
    @DisplayName("Blockbuster can sacrifice itself and its reflexive trigger resolves separately")
    void canSacrificeItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StartingColumn());
        castGastalBlockbuster();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Gastal Blockbuster"));
        harness.assertInGraveyard(player1, "Gastal Blockbuster");
        harness.handlePermanentChosen(player1, target.getId());
        harness.assertOnBattlefield(player2, "Starting Column");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Starting Column");
    }

    @Test
    @DisplayName("Sacrifice remains optional and available without any legal destruction target")
    void canSacrificeWithoutOpponentArtifact() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new StartingColumn());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HowlersHeavy());
        castGastalBlockbuster();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(harness.getPermanentId(player1, "Gastal Blockbuster"));
        assertThat(choice.validIds()).doesNotContain(ownArtifact.getId(), opponentCreature.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Gastal Blockbuster"));
        harness.assertInGraveyard(player1, "Gastal Blockbuster");
        harness.assertOnBattlefield(player1, "Starting Column");
        harness.assertOnBattlefield(player2, "Howler's Heavy");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castGastalBlockbuster() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GastalBlockbuster(), "{2}{R}");
    }
}
