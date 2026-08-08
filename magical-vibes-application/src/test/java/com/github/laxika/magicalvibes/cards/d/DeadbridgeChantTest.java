package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeadbridgeChantTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield mills ten cards")
    void entersMillsTen() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DeadbridgeChant()));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, twelveBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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

    private List<Card> twelveBears() {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            library.add(new GrizzlyBears());
        }
        return library;
    }
}
