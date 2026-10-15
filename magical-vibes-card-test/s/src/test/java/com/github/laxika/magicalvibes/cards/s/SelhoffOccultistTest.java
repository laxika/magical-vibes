package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.b.BrimstoneVolley;
import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelhoffOccultist.class, WalkingCorpse.class, BrimstoneVolley.class, BlasphemousAct.class})
class SelhoffOccultistTest extends BaseCardTest {


    @Test
    @DisplayName("When Selhoff Occultist dies, target player mills a card")
    void selfDeathMillsTargetPlayer() {
        harness.addToBattlefield(player1, new SelhoffOccultist());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new BrimstoneVolley()));
        harness.addMana(player2, ManaColor.RED, 3);

        trimDeck(player2.getId(), 10);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        UUID occultistId = harness.getPermanentId(player1, "Selhoff Occultist");
        harness.castAndResolveInstant(player2, 0, occultistId);

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player mills 1 card
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }


    @Test
    @DisplayName("When an ally creature dies, target player mills a card")
    void allyCreatureDeathMillsTargetPlayer() {
        harness.addToBattlefield(player1, new SelhoffOccultist());
        harness.addToBattlefield(player1, new WalkingCorpse());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new BrimstoneVolley()));
        harness.addMana(player2, ManaColor.RED, 3);

        trimDeck(player2.getId(), 10);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        UUID bearsId = harness.getPermanentId(player1, "Walking Corpse");
        harness.castAndResolveInstant(player2, 0, bearsId);

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player mills 1 card
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("When an opponent's creature dies, target player mills a card")
    void opponentCreatureDeathMillsTargetPlayer() {
        harness.addToBattlefield(player1, new SelhoffOccultist());
        harness.addToBattlefield(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        trimDeck(player2.getId(), 10);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        UUID bearsId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castAndResolveInstant(player1, 0, bearsId);

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player mills 1 card
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Death trigger can target the controller for mill")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player1, new SelhoffOccultist());
        harness.addToBattlefield(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        trimDeck(player1.getId(), 10);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        UUID bearsId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castAndResolveInstant(player1, 0, bearsId);

        // Choose self as target
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Controller mills 1 card
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }


    @Test
    @DisplayName("Simultaneous deaths trigger once for the Occultist and once for each other creature")
    void simultaneousDeathsMillOncePerCreature() {
        harness.addToBattlefield(player1, new SelhoffOccultist());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player2, new WalkingCorpse());
        WalkingCorpse first = new WalkingCorpse();
        WalkingCorpse second = new WalkingCorpse();
        WalkingCorpse third = new WalkingCorpse();
        WalkingCorpse fourth = new WalkingCorpse();
        harness.setLibrary(player2, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new BlasphemousAct()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player2.getId());
        }
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second, third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Milling a creature puts the top card in the graveyard without causing another death trigger")
    void millingCreatureDoesNotCauseDeathTrigger() {
        harness.addToBattlefield(player1, new SelhoffOccultist());
        WalkingCorpse top = new WalkingCorpse();
        WalkingCorpse next = new WalkingCorpse();
        harness.setLibrary(player2, List.of(top, next));
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Selhoff Occultist"));
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(next);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(top);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A death trigger can target an empty library without causing a loss")
    void emptyLibraryCanBeTargeted() {
        harness.addToBattlefield(player1, new SelhoffOccultist());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);
        int graveyardSize = gd.playerGraveyards.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Selhoff Occultist"));
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardSize);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void trimDeck(UUID playerId, int size) {
        harness.setLibrary(playerId.equals(player1.getId()) ? player1 : player2,
                List.copyOf(gd.playerDecks.get(playerId).subList(
                        gd.playerDecks.get(playerId).size() - size, gd.playerDecks.get(playerId).size())));
    }
}
