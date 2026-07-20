package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KefnetTheMindfulTest extends BaseCardTest {

    // ===== {3}{U}: Draw a card, then you may return a land you control to its owner's hand =====

    @Test
    @DisplayName("Ability draws a card, then the accepted return bounces the chosen land")
    void drawsThenReturnsChosenLand() {
        harness.addToBattlefield(player1, new KefnetTheMindful());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        UUID islandId = harness.getPermanentId(player1, "Island");
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Card is drawn first, then the "may return a land" prompt is offered to the controller.
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(islandId, forestId);

        harness.handlePermanentChosen(player1, islandId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(forestId))
                .noneMatch(p -> p.getId().equals(islandId));
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Island"));
    }

    @Test
    @DisplayName("Declining the optional return still draws a card and leaves lands in play")
    void decliningKeepsLands() {
        harness.addToBattlefield(player1, new KefnetTheMindful());
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(islandId));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getName().equals("Island"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Can't attack or block unless you have seven or more cards in hand =====

    @Test
    @DisplayName("Cannot attack with fewer than seven cards in hand")
    void cannotAttackBelowSeven() {
        addCreatureReady(player1, new KefnetTheMindful());
        harness.setHand(player1, List.of(new Island(), new Island(), new Island()));

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack with seven or more cards in hand")
    void canAttackAtSeven() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new KefnetTheMindful());
        harness.setHand(player1, List.of(new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island(), new Island()));

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot block with fewer than seven cards in hand")
    void cannotBlockBelowSeven() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new KefnetTheMindful());
        harness.setHand(player1, List.of(new Island(), new Island(), new Island()));

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block with seven or more cards in hand")
    void canBlockAtSeven() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new KefnetTheMindful());
        harness.setHand(player1, List.of(new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island(), new Island()));

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    // ===== Helpers =====

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
