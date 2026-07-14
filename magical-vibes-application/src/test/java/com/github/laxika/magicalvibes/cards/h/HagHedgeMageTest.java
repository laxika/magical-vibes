package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HagHedgeMageTest extends BaseCardTest {

    // ===== Swamp gate: may have target player discard a card =====

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
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
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

    // ===== Forest gate: may put a graveyard card on top of your library =====

    @Test
    @DisplayName("With two Forests, ETB may put a card from your graveyard on top of your library")
    void forestGatePutsGraveyardCardOnTopOfLibrary() {
        addLands(player1, 0, 2);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>());
        castHag();
        harness.passBothPriorities(); // resolve creature spell -> ETB on stack
        harness.passBothPriorities(); // resolve ETB -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the Forest trigger leaves the card in the graveyard")
    void forestGateDeclinedLeavesGraveyard() {
        addLands(player1, 0, 2);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>());
        castHag();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Neither gate met =====

    @Test
    @DisplayName("With no Swamps or Forests, neither ability triggers")
    void neitherGateTriggers() {
        castHag();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hag Hedge-Mage"));
    }

    // ===== Both gates met =====

    @Test
    @DisplayName("With two Swamps and two Forests, both abilities may resolve")
    void bothGatesResolve() {
        addLands(player1, 2, 2);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>());
        castHag();
        harness.passBothPriorities(); // resolve creature spell -> discard target prompt

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve bundled ETB -> first may prompt (discard)
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player1, true); // second may prompt (graveyard)
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Helpers =====

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
