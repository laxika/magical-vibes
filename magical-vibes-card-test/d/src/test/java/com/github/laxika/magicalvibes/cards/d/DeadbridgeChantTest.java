package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadbridgeChant.class, GrizzlyBears.class, LightningBolt.class})
class DeadbridgeChantTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield mills ten cards")
    void entersMillsTen() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, twelveBears());
        harness.castFromHand(player1, new DeadbridgeChant(), "{4}{B}{G}");
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Upkeep puts a randomly chosen creature card onto the battlefield")
    void upkeepReanimatesCreature() {
        harness.addToBattlefield(player1, new DeadbridgeChant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Upkeep puts a randomly chosen non-creature card into hand")
    void upkeepPutsNonCreatureInHand() {
        harness.addToBattlefield(player1, new DeadbridgeChant());
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lightning Bolt");
        harness.assertNotInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("Upkeep does nothing with an empty graveyard")
    void upkeepWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new DeadbridgeChant());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during the opponent's upkeep")
    void upkeepDoesNotFireOnOpponentsTurn() {
        harness.addToBattlefield(player1, new DeadbridgeChant());
        harness.setGraveyard(player1, List.of(new LightningBolt()));

        advanceToUpkeep(player2);

        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertNotInHand(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("Entering mills all remaining cards when fewer than ten are in the library")
    void entersMillsShortLibrary() {
        GrizzlyBears bear = new GrizzlyBears();
        LightningBolt bolt = new LightningBolt();
        harness.setLibrary(player1, List.of(bear, bolt));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new DeadbridgeChant());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bear, bolt);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An upkeep trigger uses the graveyard as it exists on resolution")
    void upkeepUsesGraveyardAtResolution() {
        harness.addToBattlefield(player1, new DeadbridgeChant());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bolt);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A mixed graveyard returns exactly one random card to the appropriate zone")
    void upkeepReturnsExactlyOneCardFromMixedGraveyard() {
        harness.addToBattlefield(player1, new DeadbridgeChant());
        GrizzlyBears bear = new GrizzlyBears();
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bear, bolt));
        harness.setGraveyard(player2, List.of(new LightningBolt()));
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        if (gd.playerGraveyards.get(player1.getId()).contains(bear)) {
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(bolt);
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        } else {
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bolt);
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
    }

    private List<Card> twelveBears() {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            library.add(new GrizzlyBears());
        }
        return library;
    }
}
