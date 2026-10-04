package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EscapeTunnel.class, Forest.class, GrizzlyBears.class, HillGiant.class, Island.class})
class EscapeTunnelTest extends BaseCardTest {

    @Test
    @DisplayName("Search ability sacrifices Escape Tunnel and presents only basic land cards")
    void searchPresentsOnlyBasicLands() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Escape Tunnel");
        harness.assertInGraveyard(player1, "Escape Tunnel");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getName().equals("Forest") || card.getName().equals("Island"))
                .anyMatch(card -> card.getName().equals("Forest"))
                .anyMatch(card -> card.getName().equals("Island"))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters the battlefield tapped")
    void chosenLandEntersTapped() {
        activateSearch();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest") && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Target creature with power 2 or less can't be blocked this turn")
    void makesPowerTwoCreatureUnblockable() {
        addReadyPermanent(player1, new EscapeTunnel());
        Permanent attacker = addReadyPermanent(player1, new GrizzlyBears());
        addReadyPermanent(player2, new HillGiant());

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        addReadyPermanent(player1, new EscapeTunnel());
        Permanent target = addReadyPermanent(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetLargeCreature() {
        addReadyPermanent(player1, new EscapeTunnel());
        Permanent target = addReadyPermanent(player2, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");
    }

    @Test
    @DisplayName("Search can fail to find even when basic lands are present")
    void searchCanFailToFind() {
        activateSearch();
        harness.assertNotOnBattlefield(player1, "Escape Tunnel");
        harness.assertInGraveyard(player1, "Escape Tunnel");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Search resolves with an empty library")
    void searchWithEmptyLibrary() {
        harness.addToBattlefield(player1, new EscapeTunnel());
        harness.setLibrary(player1, List.of());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Escape Tunnel");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A tapped Escape Tunnel cannot activate either ability")
    void tappedTunnelCannotActivate() {
        Permanent tunnel = harness.addToBattlefieldAndReturn(player1, new EscapeTunnel());
        Permanent target = addReadyPermanent(player1, new GrizzlyBears());
        tunnel.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Escape Tunnel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature can be targeted and the land is sacrificed immediately")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player1, new EscapeTunnel());
        Permanent target = addReadyPermanent(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Escape Tunnel");
        harness.assertInGraveyard(player1, "Escape Tunnel");
        assertThat(target.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Power above two at resolution makes the target illegal")
    void increasedPowerBeforeResolutionMakesTargetIllegal() {
        harness.addToBattlefield(player1, new EscapeTunnel());
        Permanent target = addReadyPermanent(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, 1, null, target.getId());
        target.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
        harness.assertInGraveyard(player1, "Escape Tunnel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Increasing power after resolution does not remove unblockability")
    void increasedPowerAfterResolutionStillCannotBeBlocked() {
        harness.addToBattlefield(player1, new EscapeTunnel());
        Permanent attacker = addReadyPermanent(player1, new GrizzlyBears());
        addReadyPermanent(player2, new HillGiant());
        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setPowerModifier(1);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Granting unblockability does not undo an existing block")
    void alreadyBlockedCreatureRemainsBlocked() {
        harness.addToBattlefield(player1, new EscapeTunnel());
        Permanent attacker = addReadyPermanent(player1, new GrizzlyBears());
        Permanent blocker = addReadyPermanent(player2, new HillGiant());
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        assertThat(blocker.getBlockingTargetIds()).contains(attacker.getId());

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).contains(attacker.getId());
    }

    @Test
    @DisplayName("The unblockable ability cannot target a noncreature land")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new EscapeTunnel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Escape Tunnel");
        assertThat(gd.stack).isEmpty();
    }
    private void activateSearch() {
        harness.addToBattlefield(player1, new EscapeTunnel());
        setupLibrary();
        harness.activateAbility(player1, 0, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new GrizzlyBears()));
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
