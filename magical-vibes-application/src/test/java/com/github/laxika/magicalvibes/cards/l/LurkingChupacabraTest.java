package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.b.BrazenBuccaneers;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LurkingChupacabraTest extends BaseCardTest {

    // ===== Explore land — trigger fires, target gets -2/-2 =====

    @Test
    @DisplayName("Explore with land triggers Chupacabra — opponent creature gets -2/-2")
    void exploreLandTriggersBoost() {
        harness.addToBattlefield(player1, new LurkingChupacabra());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // Put land on top of deck so explore reveals a land
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castExplorerAndResolveExplore();

        // Should be awaiting target choice for Chupacabra's trigger
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose the opponent's creature
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Opponent's creature should have -2/-2
        assertThat(opponentCreature.getPowerModifier()).isEqualTo(-2);
        assertThat(opponentCreature.getToughnessModifier()).isEqualTo(-2);
    }

    // ===== Explore non-land — trigger fires after may choice =====

    @Test
    @DisplayName("Explore with non-land triggers Chupacabra after may graveyard choice (accept)")
    void exploreNonLandAcceptTriggersBoost() {
        harness.addToBattlefield(player1, new LurkingChupacabra());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        castExplorerAndResolveExplore();

        // Should be awaiting may ability for explore graveyard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Now should be awaiting target choice for Chupacabra
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(opponentCreature.getPowerModifier()).isEqualTo(-2);
        assertThat(opponentCreature.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Explore with non-land triggers Chupacabra after may graveyard choice (decline)")
    void exploreNonLandDeclineTriggersBoost() {
        harness.addToBattlefield(player1, new LurkingChupacabra());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        castExplorerAndResolveExplore();

        // May ability choice
        harness.handleMayAbilityChosen(player1, false);

        // Now target choice for Chupacabra
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        harness.passBothPriorities();

        assertThat(opponentCreature.getPowerModifier()).isEqualTo(-2);
        assertThat(opponentCreature.getToughnessModifier()).isEqualTo(-2);
    }

    // ===== No Chupacabra — no trigger =====

    @Test
    @DisplayName("Explore without Chupacabra does not trigger -2/-2")
    void exploreWithoutChupacabraNoTrigger() {
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castExplorerAndResolveExplore();

        // No permanent choice should be awaited
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    // ===== No valid targets — trigger is skipped =====

    @Test
    @DisplayName("Explore trigger is skipped when opponent has no creatures")
    void exploreTriggerSkippedNoTargets() {
        harness.addToBattlefield(player1, new LurkingChupacabra());

        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castExplorerAndResolveExplore();

        // No target selection should be needed — trigger should be skipped
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    // ===== Cannot target own creatures =====

    @Test
    @DisplayName("Chupacabra trigger only targets opponent creatures, not own")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new LurkingChupacabra());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castExplorerAndResolveExplore();

        // Should be awaiting target choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Verify the valid targets do NOT include our own creature
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

    // ===== Explore with empty library — no trigger =====

    @Test
    @DisplayName("Explore with empty library does not trigger Chupacabra")
    void exploreEmptyLibraryNoTrigger() {
        harness.addToBattlefield(player1, new LurkingChupacabra());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        gd.playerDecks.get(player1.getId()).clear();

        castExplorerAndResolveExplore();

        // Explore with empty library does nothing, so no trigger
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    // ===== Helpers =====

    private void castExplorerAndResolveExplore() {
        harness.setHand(player1, List.of(new BrazenBuccaneers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell — ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB explore trigger
    }
}
