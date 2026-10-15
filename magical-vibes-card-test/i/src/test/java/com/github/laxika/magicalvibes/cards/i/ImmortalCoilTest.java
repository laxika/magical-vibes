package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.Blightning;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmortalCoil.class, CylianElf.class, ResoundingThunder.class, Blightning.class})
class ImmortalCoilTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability exiles two cards from the graveyard and draws a card")
    void activatedAbilityExilesTwoAndDraws() {
        harness.addToBattlefield(player1, new ImmortalCoil());
        harness.setGraveyard(player1, List.of(new CylianElf(), new CylianElf(), new CylianElf()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, gd.playerGraveyards.get(player1.getId()).stream()
                .limit(2)
                .map(Card::getId)
                .toList());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Ability can't be activated with fewer than two cards in the graveyard")
    void cannotActivateWithoutTwoGraveyardCards() {
        harness.addToBattlefield(player1, new ImmortalCoil());
        harness.setGraveyard(player1, List.of(new CylianElf()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage to the controller is prevented and exiles a card per point prevented")
    void preventsDamageAndExilesPerPoint() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ImmortalCoil());
        harness.setGraveyard(player1, List.of(new CylianElf(), new CylianElf(), new CylianElf(), new CylianElf()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ResoundingThunder()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        // 3 damage prevented (life unchanged) and 3 cards exiled from the graveyard.
        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Controller chooses which graveyard cards to exile for prevented damage")
    void choosesCardsToExileForPrevention() {
        harness.addToBattlefield(player1, new ImmortalCoil());
        Card retained = new ResoundingThunder();
        List<Card> chosen = List.of(new CylianElf(), new CylianElf(), new CylianElf());
        harness.setGraveyard(player1, List.of(retained, chosen.get(0), chosen.get(1), chosen.get(2)));
        harness.setHand(player2, List.of(new ResoundingThunder()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, chosen.stream().map(Card::getId).toList());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(retained);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(chosen);
    }

    @Test
    @DisplayName("All damage is prevented even when the graveyard cannot cover it")
    void preventsDamageGreaterThanGraveyardSize() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ImmortalCoil());
        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.setHand(player2, List.of(new ResoundingThunder()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Loss still triggers if the graveyard is refilled later in the same spell")
    void triggersWhenGraveyardIsTemporarilyEmpty() {
        harness.addToBattlefield(player1, new ImmortalCoil());
        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.setHand(player1, List.of(new CylianElf(), new CylianElf()));
        harness.setHand(player2, List.of(new Blightning()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Refilling the graveyard does not stop an already triggered loss")
    void refillingGraveyardDoesNotStopLoss() {
        harness.addToBattlefield(player1, new ImmortalCoil());
        harness.setGraveyard(player1, List.of());
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Paying the last two graveyard cards triggers loss before the draw resolves")
    void payingLastTwoCardsTriggersLossBeforeDraw() {
        harness.addToBattlefield(player1, new ImmortalCoil());
        harness.setGraveyard(player1, List.of(new CylianElf(), new CylianElf()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, gd.playerGraveyards.get(player1.getId()).stream()
                .map(Card::getId).toList());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("The draw ability taps Immortal Coil and cannot be activated again while tapped")
    void drawAbilityRequiresUntappedCoil() {
        harness.addToBattlefield(player1, new ImmortalCoil());
        harness.setGraveyard(player1, List.of(new CylianElf(), new CylianElf(), new CylianElf(), new CylianElf()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, gd.playerGraveyards.get(player1.getId()).stream()
                .limit(2).map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Immortal Coil").isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Immortal Coil does not prevent damage to its controller's opponent")
    void doesNotPreventDamageToOpponent() {
        harness.addToBattlefield(player1, new ImmortalCoil());
        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage is prevented and an emptied graveyard triggers loss")
    void preventsCombatDamage() {
        harness.addToBattlefield(player1, new ImmortalCoil());
        harness.setGraveyard(player1, List.of(new CylianElf(), new CylianElf()));
        harness.setLife(player1, 20);
        addCreatureReady(player2, new CylianElf());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Controller loses the game when their graveyard is empty")
    void losesGameWhenGraveyardEmpty() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ImmortalCoil());
        harness.setGraveyard(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.runStateBasedActions();
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
