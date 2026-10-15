package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HagHedgeMage.class, Forest.class, Swamp.class, GrizzlyBears.class})
class HagHedgeMageTest extends BaseCardTest {

    @Test
    @DisplayName("With two Swamps, ETB may make target player discard a card")
    void swampGateMakesTargetPlayerDiscard() {
        addLands(player1, 2, 0);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castHag();
        harness.passBothPriorities(); // resolve creature spell -> discard target prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB -> may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the Swamp trigger leaves the target player's hand intact")
    void swampGateDeclinedDoesNotDiscard() {
        addLands(player1, 2, 0);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castHag();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("With only one Swamp the discard trigger does not fire (no target prompt)")
    void oneSwampDoesNotTrigger() {
        addLands(player1, 1, 0);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castHag();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("With two Forests, ETB may put a card from your graveyard on top of your library")
    void forestGatePutsGraveyardCardOnTopOfLibrary() {
        addLands(player1, 0, 2);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>());
        castHag();
        harness.passBothPriorities();
        chooseForestTarget();
        harness.passBothPriorities(); // resolve ETB -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the Forest trigger leaves the card in the graveyard")
    void forestGateDeclinedLeavesGraveyard() {
        addLands(player1, 0, 2);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>());
        castHag();
        harness.passBothPriorities();
        chooseForestTarget();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("With only one Forest the graveyard trigger does not fire")
    void oneForestDoesNotTrigger() {
        addLands(player1, 0, 1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        castHag();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("With no Swamps or Forests, neither ability triggers")
    void neitherGateTriggers() {
        castHag();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Hag Hedge-Mage");
    }

    @Test
    @DisplayName("Both land conditions create separate triggered abilities")
    void bothGatesCreateSeparateTriggers() {
        addLands(player1, 2, 2);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castHag();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        chooseForestTarget();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void emptyGraveyardCannotSupplyForestTarget() {
        addLands(player1, 0, 2);
        harness.setGraveyard(player1, List.of());
        castHag();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void forestTargetLeavingGraveyardDoesNotAllowAnotherCardToBeChosen() {
        addLands(player1, 0, 2);
        GrizzlyBears target = new GrizzlyBears();
        GrizzlyBears other = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setLibrary(player1, List.of());
        castHag();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    void swampConditionIsCheckedAgainOnResolution() {
        addLands(player1, 2, 0);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        castHag();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Swamp);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void forestConditionIsCheckedAgainOnResolution() {
        addLands(player1, 0, 2);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of());
        castHag();
        harness.passBothPriorities();
        chooseForestTarget();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Forest);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void chooseForestTarget() {
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(
                gd.playerGraveyards.get(player1.getId()).getFirst().getId());
        harness.handleMultipleCardsChosen(player1, List.of(
                gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
    }

    private void castHag() {
        harness.setHand(player1, List.of(new HagHedgeMage()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
    }

    private void addLands(Player player, int swamps, int forests) {
        for (int i = 0; i < swamps; i++) {
            harness.addToBattlefield(player, new Swamp());
        }
        for (int i = 0; i < forests; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
