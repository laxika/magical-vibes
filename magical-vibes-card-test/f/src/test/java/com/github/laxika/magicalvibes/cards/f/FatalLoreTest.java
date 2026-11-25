package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.cards.e.EnlistedWurm;
import com.github.laxika.magicalvibes.cards.g.GorillaChieftain;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FatalLore.class, ElvishRanger.class, GorillaChieftain.class, DeadlyInsect.class, WhiteKnight.class,
        EnlistedWurm.class})
class FatalLoreTest extends BaseCardTest {

    private static final String FIRST_MODE = "You draw three cards";
    private static final String SECOND_MODE =
            "You destroy up to two target creatures that player controls; that player draws up to three cards";

    @Test
    @DisplayName("The opponent chooses a mode during casting before priority is passed")
    void castingPromptsOpponentChoice() {
        harness.castFromHand(player1, new FatalLore(), "{2}{B}{B}");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
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

        harness.handleListChoice(player2, FIRST_MODE);
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

        harness.handleListChoice(player2, SECOND_MODE);
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
        harness.handleListChoice(player2, SECOND_MODE);
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

        harness.handleListChoice(player2, SECOND_MODE);
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

        harness.handleListChoice(player2, SECOND_MODE);
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

        harness.handleListChoice(player2, SECOND_MODE);

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

        harness.handleListChoice(player2, SECOND_MODE);
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

        harness.handleListChoice(player2, SECOND_MODE);

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

        harness.handleListChoice(player2, SECOND_MODE);

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

        harness.handleListChoice(player2, SECOND_MODE);
        harness.handleMultiplePermanentsChosen(player1, List.of(ranger.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(ranger);
        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getName().equals("Fatal Lore"));
    }

    @Test
    @DisplayName("Second mode with two opponents: targets and the draw belong to the opponent who chose it")
    void secondModeUsesTheChoosingOpponent() {
        Player player3 = addThirdPlayer();
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        Permanent third = harness.addToBattlefieldAndReturn(player3, new ElvishRanger());
        stockLibrary(player3);
        setupAndCast();
        GameData gd = harness.getGameData();

        harness.handlePermanentChosen(player1, player3.getId());
        harness.handleListChoice(player3, SECOND_MODE);

        PendingInteraction.MultiPermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(third.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        PendingInteraction.XValueChoice draw = gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(draw.playerId()).isEqualTo(player3.getId());
    }

    @Test
    @DisplayName("Cast for free from exile by cascade, the opponent still chooses its mode before priority")
    void freeCastFromCascadeStillAsksTheOpponentForTheMode() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of(new FatalLore(), new ElvishRanger()));

        harness.castFromHand(player1, new EnlistedWurm(), "{4}{G}{W}");
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Fatal Lore"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
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

    private Player addThirdPlayer() {
        UUID id = UUID.randomUUID();
        Player player = new Player(id, "Charlie");
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), id, "Charlie");
        return player;
    }
}
