package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SentinelTotem.class, RaptorCompanion.class, DualShot.class})
class SentinelTotemTest extends BaseCardTest {

    @Test
    @DisplayName("Casting and resolving Sentinel Totem triggers scry 1")
    void castingTriggersScry() {
        harness.castFromHand(player1, new SentinelTotem(), "{1}");
        harness.passBothPriorities(); // resolve artifact

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Sentinel Totem");

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Resolving ETB enters scry state with 1 card")
    void resolvingEtbEntersScryState() {
        harness.castFromHand(player1, new SentinelTotem(), "{1}");
        harness.passBothPriorities(); // resolve artifact
        harness.passBothPriorities(); // resolve ETB

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Activating ability exiles Sentinel Totem as cost and puts ability on stack")
    void activatingExilesSelfAndPutsOnStack() {
        addReadyTotem(player1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        // Totem is exiled as cost
        harness.assertNotOnBattlefield(player1, "Sentinel Totem");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sentinel Totem"));
        // Ability is on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving ability exiles all cards from all graveyards")
    void resolvingExilesAllGraveyards() {
        addReadyTotem(player1);

        // Put cards in both players' graveyards
        RaptorCompanion bears = new RaptorCompanion();
        DualShot bolt = new DualShot();
        harness.setGraveyard(player1, List.of(bears));
        harness.setGraveyard(player2, List.of(bolt));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Both graveyards should be empty
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // Cards should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Raptor Companion"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Dual Shot"));
    }

    @Test
    @DisplayName("Resolving ability with empty graveyards does not error")
    void resolvingWithEmptyGraveyards() {
        addReadyTotem(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sentinel Totem goes to exile, not graveyard, as cost")
    void totemGoesToExileNotGraveyard() {
        addReadyTotem(player1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Sentinel Totem");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sentinel Totem"));
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent totem = addReadyTotem(player1);
        totem.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles only the opponent's graveyard when controller's graveyard is empty")
    void exilesOnlyOpponentGraveyard() {
        addReadyTotem(player1);

        DualShot bolt = new DualShot();
        harness.setGraveyard(player2, List.of(bolt));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Dual Shot"));
    }

    @Test
    @DisplayName("Exiles multiple cards from a single graveyard")
    void exilesMultipleCardsFromSingleGraveyard() {
        addReadyTotem(player1);

        RaptorCompanion bears = new RaptorCompanion();
        DualShot bolt = new DualShot();
        harness.setGraveyard(player2, List.of(bears, bolt));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Raptor Companion"))
                .anyMatch(c -> c.getName().equals("Dual Shot"));
    }

    @Test
    @DisplayName("Scry can keep the top card without changing either library's order")
    void scryKeepsTopCard() {
        RaptorCompanion top = new RaptorCompanion();
        DualShot next = new DualShot();
        SentinelTotem opposingTop = new SentinelTotem();
        harness.setLibrary(player1, List.of(top, next));
        harness.setLibrary(player2, List.of(opposingTop));
        harness.castFromHand(player1, new SentinelTotem(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingTop);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom without drawing")
    void scryBottomsTopCard() {
        RaptorCompanion top = new RaptorCompanion();
        DualShot next = new DualShot();
        harness.setLibrary(player1, List.of(top, next));
        harness.castFromHand(player1, new SentinelTotem(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Scry with an empty library completes without asking for input")
    void scryWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new SentinelTotem(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sentinel Totem");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Totem can activate immediately in response to its own scry trigger")
    void activatesBeforeScryResolves() {
        RaptorCompanion top = new RaptorCompanion();
        DualShot graveyardCard = new DualShot();
        harness.setLibrary(player1, List.of(top));
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.castFromHand(player1, new SentinelTotem(), "{1}");
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Sentinel Totem");
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exile includes cards placed in graveyards after activation")
    void exilesGraveyardsAtResolution() {
        addReadyTotem(player1);
        RaptorCompanion initialCard = new RaptorCompanion();
        DualShot laterCard = new DualShot();
        harness.setGraveyard(player1, List.of(initialCard));
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(initialCard);
        harness.setGraveyard(player2, List.of(laterCard));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(initialCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(laterCard);
    }

    private Permanent addReadyTotem(Player player) {
        return addCreatureReady(player, new SentinelTotem());
    }
}
