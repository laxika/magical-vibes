package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.e.EmeraldCharm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PillarTombsOfAku.class, Warthog.class, LeylineOfSanctity.class, EmeraldCharm.class})
class PillarTombsOfAkuTest extends BaseCardTest {

    @Test
    @DisplayName("Controller declines: loses 5 life and Pillar Tombs is sacrificed")
    void controllerDeclinesLosesLifeAndSacrifices() {
        harness.addToBattlefield(player1, new PillarTombsOfAku());
        harness.addToBattlefield(player1, new Warthog());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Pillar Tombs of Aku");
        harness.assertOnBattlefield(player1, "Warthog");
    }

    @Test
    @DisplayName("Controller accepts with one creature: sacrifices it, Pillar Tombs survives")
    void controllerAcceptsSacrificesCreature() {
        harness.addToBattlefield(player1, new PillarTombsOfAku());
        harness.addToBattlefield(player1, new Warthog());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Warthog");
        harness.assertOnBattlefield(player1, "Pillar Tombs of Aku");
    }

    @Test
    @DisplayName("Controller accepts with multiple creatures: chooses which to sacrifice")
    void controllerAcceptsChoosesAmongCreatures() {
        harness.addToBattlefield(player1, new PillarTombsOfAku());
        harness.addToBattlefield(player1, new Warthog());
        harness.addToBattlefield(player1, new Warthog());
        UUID chosen = harness.getPermanentId(player1, "Warthog");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, chosen);

        assertThat(countPermanents(player1, "Warthog")).isEqualTo(1);
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
    @DisplayName("The upkeep penalty does not target the player")
    void penaltyAppliesToPlayerWithHexproof() {
        harness.addToBattlefield(player1, new PillarTombsOfAku());
        harness.addToBattlefield(player2, new LeylineOfSanctity());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        harness.assertNotOnBattlefield(player1, "Pillar Tombs of Aku");
    }

    @Test
    @DisplayName("Opponent declines on their upkeep: opponent loses 5, controller sacrifices Pillar Tombs")
    void opponentDeclinesOpponentLosesLifeControllerSacrifices() {
        harness.addToBattlefield(player1, new PillarTombsOfAku());
        harness.addToBattlefield(player2, new Warthog());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Pillar Tombs of Aku");
        harness.assertOnBattlefield(player2, "Warthog");
    }

    @Test
    @DisplayName("Opponent accepts on their upkeep: opponent sacrifices a creature, Pillar Tombs survives")
    void opponentAcceptsSacrificesTheirCreature() {
        harness.addToBattlefield(player1, new PillarTombsOfAku());
        harness.addToBattlefield(player2, new Warthog());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player2, "Warthog");
        harness.assertOnBattlefield(player1, "Pillar Tombs of Aku");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Removing Pillar Tombs in response does not prevent the upkeep life loss")
    void sourceRemovedBeforeResolutionStillLosesLife() {
        Permanent tombs = harness.addToBattlefieldAndReturn(player1, new PillarTombsOfAku());
        harness.setHand(player2, List.of(new EmeraldCharm()));

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castModalInstant(player2, 0, 1, List.of(tombs.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Pillar Tombs of Aku");
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("After Pillar Tombs is removed, sacrificing a creature still avoids the penalty")
    void sourceRemovedBeforeResolutionCanStillSacrifice() {
        Permanent tombs = harness.addToBattlefieldAndReturn(player1, new PillarTombsOfAku());
        harness.addToBattlefield(player2, new Warthog());
        harness.setHand(player2, List.of(new EmeraldCharm()));

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castModalInstant(player2, 0, 1, List.of(tombs.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Pillar Tombs of Aku");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Warthog");
        harness.assertNotOnBattlefield(player2, "Warthog");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }
}
