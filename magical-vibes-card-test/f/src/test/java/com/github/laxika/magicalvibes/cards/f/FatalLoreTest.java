package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.cards.g.GorillaChieftain;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FatalLore.class, ElvishRanger.class, GorillaChieftain.class, DeadlyInsect.class, WhiteKnight.class})
class FatalLoreTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent chooses a mode during casting before priority is passed")
    void castingPromptsOpponentChoice() {
        harness.castFromHand(player1, new FatalLore(), "{2}{B}{B}");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Accept mode: the controller draws three cards")
    void acceptDrawsThreeCards() {
        stockLibrary(player1);
        setupAndCast();
        GameData gd = harness.getGameData();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Decline mode: the controller destroys up to two of that player's creatures, then they draw up to three")
    void declineDestroysTwoCreaturesThenOpponentDraws() {
        Permanent rangerA = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        Permanent rangerB = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        Permanent rangerC = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        stockLibrary(player2);
        setupAndCast();
        GameData gd = harness.getGameData();

        harness.handleMayAbilityChosen(player2, false);
        harness.handleMultiplePermanentsChosen(player1, List.of(rangerA.getId(), rangerB.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(rangerC);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleXValueChosen(player2, 3);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 3);
    }

    @Test
    @DisplayName("Decline mode: destroyed creatures can't be regenerated")
    void declineDestroyIgnoresRegeneration() {
        Permanent chieftain = harness.addToBattlefieldAndReturn(player2, new GorillaChieftain());
        stockLibrary(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.regenerationShieldCount(chieftain.getId())).isEqualTo(1);

        setupAndCast();
        GameData gd = harness.getGameData();
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMultiplePermanentsChosen(player1, List.of(chieftain.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Decline mode: choosing no creatures still lets that player draw up to three")
    void declineWithNoCreaturesChosen() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        stockLibrary(player2);
        setupAndCast();
        GameData gd = harness.getGameData();

        harness.handleMayAbilityChosen(player2, false);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(ranger);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Decline mode with no creatures on the opponent's battlefield still draws them up to three")
    void declineWithEmptyBattlefield() {
        stockLibrary(player2);
        setupAndCast();
        GameData gd = harness.getGameData();

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The destroy mode cannot select a creature with shroud")
    void declineCannotTargetShroud() {
        Permanent insect = harness.addToBattlefieldAndReturn(player2, new DeadlyInsect());
        Permanent ranger = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        setupAndCast();

        harness.handleMayAbilityChosen(player2, false);

        PendingInteraction.MultiPermanentChoice choice = harness.getGameData().interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(ranger.getId()).doesNotContain(insect.getId());
    }

    @Test
    @DisplayName("Destroying one creature lets the opponent choose to draw two cards")
    void declineDestroysOneCreatureAndDrawsTwo() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        stockLibrary(player2);
        setupAndCast();

        harness.handleMayAbilityChosen(player2, false);
        harness.handleMultiplePermanentsChosen(player1, List.of(ranger.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player2.getId()).size();
        int deckBefore = gd.playerDecks.get(player2.getId()).size();
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(ranger.getCard());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Decline mode: the caster chooses the targets right after the mode, before anyone gets priority")
    void declineAsksForTargetsBeforePriority() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        setupAndCast();

        harness.handleMayAbilityChosen(player2, false);

        PendingInteraction.MultiPermanentChoice choice = harness.getGameData().interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(ranger.getId());
        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Decline mode: a creature with protection from black can't be chosen as a target")
    void declineCannotTargetProtectionFromBlack() {
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new WhiteKnight());
        Permanent ranger = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        setupAndCast();

        harness.handleMayAbilityChosen(player2, false);

        PendingInteraction.MultiPermanentChoice choice = harness.getGameData().interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(ranger.getId()).doesNotContain(knight.getId());
    }

    @Test
    @DisplayName("Decline mode: when every chosen target is gone, the spell doesn't resolve and that player draws nothing")
    void declineWithEveryTargetGoneDoesNothing() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        stockLibrary(player2);
        setupAndCast();
        GameData gd = harness.getGameData();

        harness.handleMayAbilityChosen(player2, false);
        harness.handleMultiplePermanentsChosen(player1, List.of(ranger.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(ranger);
        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getName().equals("Fatal Lore"));
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new FatalLore(), "{2}{B}{B}");
    }

    private void stockLibrary(Player player) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            deck.add(new ElvishRanger());
        }
        harness.setLibrary(player, deck);
    }
}
