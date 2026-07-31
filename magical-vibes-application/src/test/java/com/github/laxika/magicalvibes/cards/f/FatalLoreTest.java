package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FatalLoreTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving prompts the opponent to choose a mode")
    void resolvingPromptsOpponentChoice() {
        setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Accept mode: the controller draws three cards")
    void acceptDrawsThreeCards() {
        setupAndCast();
        stockLibrary(player1.getId());
        GameData gd = harness.getGameData();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Decline mode: the controller destroys up to two of that player's creatures, then they draw up to three")
    void declineDestroysTwoCreaturesThenOpponentDraws() {
        setupAndCast();
        stockLibrary(player2.getId());
        GameData gd = harness.getGameData();

        Permanent bearA = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bearB = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bearC = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(bearA.getId(), bearB.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bearC);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleXValueChosen(player2, 3);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 3);
    }

    @Test
    @DisplayName("Decline mode: destroyed creatures can't be regenerated")
    void declineDestroyIgnoresRegeneration() {
        setupAndCast();
        stockLibrary(player2.getId());
        GameData gd = harness.getGameData();

        Permanent skeleton = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, 0, null, null);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(skeleton.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Decline mode: choosing no creatures still lets that player draw up to three")
    void declineWithNoCreaturesChosen() {
        setupAndCast();
        stockLibrary(player2.getId());
        GameData gd = harness.getGameData();

        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bear);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Decline mode with no creatures on the opponent's battlefield still draws them up to three")
    void declineWithEmptyBattlefield() {
        setupAndCast();
        stockLibrary(player2.getId());
        GameData gd = harness.getGameData();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new FatalLore()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, 0);
    }

    private void stockLibrary(java.util.UUID playerId) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            deck.add(new GrizzlyBears());
        }
        harness.getGameData().playerDecks.get(playerId).clear();
        harness.getGameData().playerDecks.get(playerId).addAll(deck);
    }
}
