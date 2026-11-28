package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DecoyGambit.class, GrizzlyBears.class, NarsetParterOfVeils.class, ControlMagic.class})
class DecoyGambitTest extends BaseCardTest {

    @Test
    @DisplayName("The target controller declines and the creature returns to its owner's hand")
    void declinesAndReturnsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The target controller has the caster draw and the creature stays")
    void acceptsDrawInsteadOfReturningCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        cast(target);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot choose two creatures controlled by the same opponent")
    void allowsAtMostOneTargetPerOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecoyGambit()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("Can be cast with no targets and returns nothing")
    void canChooseNoTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecoyGambit()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Decoy Gambit");
    }

    @Test
    @DisplayName("Cannot target a creature its caster controls")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecoyGambit()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that left the battlefield offers no draw choice")
    void missingTargetDoesNotOfferDrawChoice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecoyGambit()));
        addMana();
        harness.castInstant(player1, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Decoy Gambit");
    }

    @Test
    @DisplayName("Returns the creature to its owner's hand, not its controller's")
    void returnsCreatureToOwnerRatherThanController() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new ControlMagic()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        gd.activePlayerId = player2.getId();
        harness.castEnchantment(player2, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);

        cast(target);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponents choose in turn order, not in target order")
    void choicesFollowTurnOrderRatherThanTargetOrder() {
        Player third = addOpponent();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(third, new GrizzlyBears());

        castMultiplayer(List.of(second.getId(), first.getId()));

        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(third.getId());
        harness.handleMayAbilityChosen(third, false);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(third, "Grizzly Bears");
    }

    @Test
    @DisplayName("The caster draws only after every opponent has chosen")
    void drawsWaitUntilAllOpponentsHaveChosen() {
        Player third = addOpponent();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(third, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castMultiplayer(List.of(first.getId(), second.getId()));

        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(third.getId())).contains(second);
        harness.handleMayAbilityChosen(third, false);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(third, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first);
    }

    @Test
    @DisplayName("Creatures return only after every opponent has chosen")
    void returnsWaitUntilAllOpponentsHaveChosen() {
        Player third = addOpponent();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(third, new GrizzlyBears());
        castMultiplayer(List.of(first.getId(), second.getId()));

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first);
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.handleMayAbilityChosen(third, false);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(third, "Grizzly Bears");
    }

    @Test
    @DisplayName("A target that changed to another opponent's control is illegal")
    void changingToAnotherOpponentMakesTargetIllegal() {
        Player third = addOpponent();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecoyGambit()));
        addMana();
        harness.castInstant(player1, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(third.getId()).add(target);

        resolveTopOfStack();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(third.getId())).contains(target);
        harness.assertInGraveyard(player1, "Decoy Gambit");
    }

    @Test
    @DisplayName("The draw can't be chosen when the caster can't draw, so the creature returns")
    void prohibitedDrawCannotBeChosenInsteadOfReturningCreature() {
        harness.addToBattlefieldAndReturn(player2, new NarsetParterOfVeils())
                .setCounterCount(CounterType.LOYALTY, 5);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.setHand(player1, List.of(new DecoyGambit()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target);

        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player2, true);
        }
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A later opponent can't choose a draw past the caster's limit after an earlier opponent chose one")
    void drawChosenByEarlierOpponentCountsTowardDrawLimit() {
        Player third = addOpponent();
        harness.addToBattlefieldAndReturn(player2, new NarsetParterOfVeils())
                .setCounterCount(CounterType.LOYALTY, 5);
        gd.cardsDrawnThisTurn.remove(player1.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(third, new GrizzlyBears());
        castMultiplayer(List.of(first.getId(), second.getId()));

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first);
        harness.assertInHand(third, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new DecoyGambit()));
        addMana();
        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
    }

    private void castMultiplayer(List<UUID> targets) {
        harness.setHand(player1, List.of(new DecoyGambit()));
        addMana();
        harness.castInstant(player1, 0, targets);
        resolveTopOfStack();
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private Player addOpponent() {
        Player opponent = new Player(UUID.randomUUID(), "Charlie");
        gd.playerIds.add(opponent.getId());
        gd.orderedPlayerIds.add(opponent.getId());
        gd.playerNames.add(opponent.getUsername());
        gd.playerIdToName.put(opponent.getId(), opponent.getUsername());
        gd.playerDecks.put(opponent.getId(), new ArrayList<>());
        gd.playerHands.put(opponent.getId(), new ArrayList<>());
        gd.playerGraveyards.put(opponent.getId(), new ArrayList<>());
        gd.playerBattlefields.put(opponent.getId(), new ArrayList<>());
        gd.playerCommandZones.put(opponent.getId(), new ArrayList<>());
        gd.playerManaPools.put(opponent.getId(), new ManaPool());
        gd.playerLifeTotals.put(opponent.getId(), 20);
        return opponent;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
