package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.b.BrazenBuccaneers;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RavenousDaggertooth;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.j.JadeGuardian;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkingChupacabra.class, BrazenBuccaneers.class, Forest.class, RavenousDaggertooth.class, ColossalDreadmaw.class, JadeGuardian.class})
class LurkingChupacabraTest extends BaseCardTest {


    @Test
    @DisplayName("Explore with land triggers Chupacabra — opponent creature gets -2/-2")
    void exploreLandTriggersBoost() {
        harness.addToBattlefield(player1, new LurkingChupacabra());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RavenousDaggertooth());

        // Put land on top of deck so explore reveals a land
        harness.setLibrary(player1, List.of(new Forest()));

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


    @Test
    @DisplayName("Explore with non-land triggers Chupacabra after may graveyard choice (accept)")
    void exploreNonLandAcceptTriggersBoost() {
        harness.addToBattlefield(player1, new LurkingChupacabra());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RavenousDaggertooth());

        harness.setLibrary(player1, List.of(new RavenousDaggertooth()));

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
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RavenousDaggertooth());

        harness.setLibrary(player1, List.of(new RavenousDaggertooth()));

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


    @Test
    @DisplayName("Explore without Chupacabra does not trigger -2/-2")
    void exploreWithoutChupacabraNoTrigger() {
        harness.addToBattlefieldAndReturn(player2, new RavenousDaggertooth());

        harness.setLibrary(player1, List.of(new Forest()));

        castExplorerAndResolveExplore();

        // No permanent choice should be awaited
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }


    @Test
    @DisplayName("Explore trigger is skipped when opponent has no creatures")
    void exploreTriggerSkippedNoTargets() {
        harness.addToBattlefield(player1, new LurkingChupacabra());

        harness.setLibrary(player1, List.of(new Forest()));

        castExplorerAndResolveExplore();

        // No target selection should be needed — trigger should be skipped
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }


    @Test
    @DisplayName("Chupacabra trigger only targets opponent creatures, not own")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new LurkingChupacabra());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RavenousDaggertooth());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RavenousDaggertooth());

        harness.setLibrary(player1, List.of(new Forest()));

        castExplorerAndResolveExplore();

        // Should be awaiting target choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Verify the valid targets do NOT include our own creature
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());
    }


    @Test
    @DisplayName("Explore with empty library still triggers Chupacabra")
    void exploreEmptyLibraryTriggersBoost() {
        harness.addToBattlefield(player1, new LurkingChupacabra());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RavenousDaggertooth());

        harness.setLibrary(player1, List.of());

        castExplorerAndResolveExplore();

        // Exploring an empty library still produces an explore event.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Ravenous Daggertooth");
        harness.assertInGraveyard(player2, "Ravenous Daggertooth");
    }


    @Test
    void cannotChooseOpponentHexproofCreature() {
        harness.addToBattlefield(player1, new LurkingChupacabra());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new JadeGuardian());
        Permanent legalCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setLibrary(player1, List.of(new Forest()));
        castExplorerAndResolveExplore();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(legalCreature.getId())
                .doesNotContain(protectedCreature.getId());
    }

    @Test
    void reductionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new LurkingChupacabra());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setLibrary(player1, List.of(new Forest()));
        castExplorerAndResolveExplore();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void targetBecomingControlledByAbilityControllerIsIllegal() {
        harness.addToBattlefield(player1, new LurkingChupacabra());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setLibrary(player1, List.of(new Forest()));
        castExplorerAndResolveExplore();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void opponentsExploreDoesNotTrigger() {
        harness.addToBattlefield(player1, new LurkingChupacabra());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.forceActivePlayer(player2);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.castFromHand(player2, new BrazenBuccaneers(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castExplorerAndResolveExplore() {
        harness.castFromHand(player1, new BrazenBuccaneers(), "{3}{R}");
        harness.passBothPriorities(); // resolve creature spell — ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB explore trigger
    }
}
