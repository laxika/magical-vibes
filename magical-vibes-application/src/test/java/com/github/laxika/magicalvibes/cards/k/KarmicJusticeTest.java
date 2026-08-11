package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KarmicJusticeTest extends BaseCardTest {

    private void resolveDemolish(UUID targetId) {
        harness.setHand(player2, List.of(new Demolish()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player2, 0, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Opponent destruction triggers a may-destroy ability for their permanent")
    void opponentDestroysNoncreaturePermanent() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        resolveDemolish(fountainId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the triggered ability does not destroy the target")
    void mayBeDeclined() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        resolveDemolish(fountainId);

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroying Karmic Justice itself still triggers its ability")
    void destroyingKarmicJusticeTriggersItsAbility() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID karmicJusticeId = harness.getPermanentId(player1, "Karmic Justice");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player2, 0, karmicJusticeId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Karmic Justice");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mass destruction also triggers Karmic Justice")
    void massDestructionTriggersKarmicJustice() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of(new PlanarCleansing()));
        harness.addMana(player2, ManaColor.WHITE, 6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forestId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Karmic Justice");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("A creature destroyed by damage does not trigger Karmic Justice")
    void creatureDestroyedByDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
