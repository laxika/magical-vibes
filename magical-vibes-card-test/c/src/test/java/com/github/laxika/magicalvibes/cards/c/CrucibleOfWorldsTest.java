package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrucibleOfWorlds.class, Forest.class, GrizzlyBears.class, Plains.class})
class CrucibleOfWorldsTest extends BaseCardTest {

    @Test
    @DisplayName("Crucible of Worlds can be cast as an artifact")
    void canBeCast() {
        harness.setHand(player1, List.of(new CrucibleOfWorlds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Crucible of Worlds");
    }

    @Test
    @DisplayName("Can play a land from graveyard with Crucible on battlefield")
    void canPlayLandFromGraveyard() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Playing land from graveyard counts as the land play for the turn")
    void usesNormalLandDrop() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest(), new Plains()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Play first land from graveyard
        harness.playGraveyardLand(player1, 0);

        // Second graveyard land should not be playable
        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Cannot play land from graveyard if already played a land from hand")
    void cannotPlayIfAlreadyPlayedFromHand() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        Plains handPlains = new Plains();
        harness.setHand(player1, List.of(handPlains));
        harness.setGraveyard(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Play a land from hand first
        harness.playLand(player1, 0);

        // Graveyard land should not be playable
        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Cannot play land from hand if already played from graveyard")
    void cannotPlayFromHandIfAlreadyPlayedFromGraveyard() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Plains()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Play a land from graveyard first
        harness.playGraveyardLand(player1, 0);

        // Hand land should not be playable
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot play land from graveyard without Crucible on battlefield")
    void cannotPlayWithoutCrucible() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Removing Crucible disables playing lands from graveyard")
    void removingCrucibleDisablesAbility() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Remove Crucible from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Crucible of Worlds"));

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Cannot play land from graveyard when Crucible has lost all abilities")
    void cannotPlayWhenCrucibleHasLostAllAbilities() {
        Permanent crucible = harness.addToBattlefieldAndReturn(player1, new CrucibleOfWorlds());
        crucible.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Creatures in graveyard are not playable via Crucible")
    void creaturesInGraveyardNotPlayable() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Cannot play land from graveyard during opponent's turn")
    void cannotPlayDuringOpponentTurn() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Cannot play land from graveyard during combat")
    void cannotPlayDuringCombat() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Crucible only allows its controller to play lands from graveyard")
    void onlyAffectsController() {
        // Player1 controls Crucible, but player2 tries to play from their graveyard
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setHand(player2, List.of());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Can still play lands from hand normally with Crucible on battlefield")
    void canStillPlayFromHand() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setHand(player1, List.of(new Plains()));
        harness.setGraveyard(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Can play land from graveyard during postcombat main phase")
    void worksInPostcombatMain() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot play a graveyard land while a spell is on the stack")
    void cannotPlayWithNonemptyStack() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");

        harness.passBothPriorities();
        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple Crucibles do not grant additional land plays")
    void multipleCruciblesDoNotGrantExtraLandPlays() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest(), new Plains()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
        harness.assertInGraveyard(player1, "Plains");
    }

    @Test
    @DisplayName("Crucible cannot play lands from an opponent's graveyard by card ID")
    void cannotPlayOpponentGraveyardLand() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        Forest forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Plays the correct land when multiple cards are in graveyard")
    void playsCorrectLandFromGraveyard() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setGraveyard(player1, List.of(bears, forest, plains));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Index 1 is the Forest (index 0 is GrizzlyBears which is not a land)
        harness.playGraveyardLand(player1, 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Forest");
    }
}

