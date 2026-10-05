package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeurokProdigy.class, DarksteelIngot.class, CrazedGoblin.class})
class NeurokProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Neurok Prodigy's discard cost only offers artifact cards from hand")
    void discardCostOffersOnlyArtifactCards() {
        harness.addToBattlefield(player1, new NeurokProdigy());
        harness.setHand(player1, List.of(new CrazedGoblin(), new DarksteelIngot()));

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.DiscardCostChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class);
        assertThat(discardChoice.validIndices()).containsExactly(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discarding an artifact card puts Neurok Prodigy's return ability on the stack")
    void artifactDiscardCostPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new NeurokProdigy());
        harness.setHand(player1, List.of(new DarksteelIngot()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        harness.assertInGraveyard(player1, "Darksteel Ingot");
    }

    @Test
    @DisplayName("Discarding an artifact card returns Neurok Prodigy to its owner's hand")
    void artifactDiscardCostReturnsNeurokProdigyToHand() {
        harness.addToBattlefield(player1, new NeurokProdigy());
        harness.setHand(player1, List.of(new DarksteelIngot()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Darksteel Ingot");
        harness.assertInHand(player1, "Neurok Prodigy");
        harness.assertNotOnBattlefield(player1, "Neurok Prodigy");
    }

    @Test
    @DisplayName("Neurok Prodigy cannot discard a nonartifact card as its activation cost")
    void cannotDiscardNonartifactCard() {
        harness.addToBattlefield(player1, new NeurokProdigy());
        harness.setHand(player1, List.of(new CrazedGoblin()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neurok Prodigy cannot activate without an artifact card in hand")
    void cannotActivateWithoutArtifactCardInHand() {
        harness.addToBattlefield(player1, new NeurokProdigy());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, newly entered Neurok Prodigy can activate its return ability")
    void tappedProdigyCanReturnToHand() {
        var prodigy = harness.addToBattlefieldAndReturn(player1, new NeurokProdigy());
        prodigy.setTapped(true);
        harness.setHand(player1, List.of(new DarksteelIngot()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Neurok Prodigy");
        harness.assertInGraveyard(player1, "Darksteel Ingot");
        harness.assertNotOnBattlefield(player1, "Neurok Prodigy");
    }

    @Test
    @DisplayName("A stolen Neurok Prodigy returns to its owner after its controller pays the cost")
    void stolenProdigyReturnsToOwner() {
        var prodigy = harness.addToBattlefieldAndReturn(player1, new NeurokProdigy());
        gd.stolenCreatures.put(prodigy.getId(), player2.getId());
        harness.setHand(player1, List.of(new DarksteelIngot()));
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Neurok Prodigy");
        harness.assertNotInHand(player1, "Neurok Prodigy");
        harness.assertInGraveyard(player1, "Darksteel Ingot");
        harness.assertNotInGraveyard(player2, "Darksteel Ingot");
        harness.assertNotOnBattlefield(player1, "Neurok Prodigy");
    }

    @Test
    @DisplayName("Multiple activations return only their source and still consume both discard costs")
    void repeatedActivationsDoNotReturnAnotherProdigy() {
        harness.addToBattlefield(player1, new NeurokProdigy());
        var otherProdigy = harness.addToBattlefieldAndReturn(player1, new NeurokProdigy());
        harness.setHand(player1, List.of(new DarksteelIngot(), new DarksteelIngot()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(otherProdigy);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Neurok Prodigy");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Darksteel Ingot");
    }
}
