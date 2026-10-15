package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GratefulApparition;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlphinaudLeveilleur.class, AlisaieLeveilleur.class, GratefulApparition.class})
class AlphinaudLeveilleurTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Alisaie")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card alisaie = new AlisaieLeveilleur();
        harness.setLibrary(player2, List.of(alisaie));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new AlphinaudLeveilleur());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(alisaie);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The second spell each turn draws a card")
    void secondSpellEachTurnDrawsCard() {
        harness.addToBattlefield(player1, new AlphinaudLeveilleur());
        harness.setHand(player1, List.of(new GratefulApparition(), new GratefulApparition(), new GratefulApparition()));
        harness.setLibrary(player1, List.of(new GratefulApparition(), new GratefulApparition()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The targeted player can decline the partner search")
    void targetPlayerCanDeclinePartnerSearch() {
        Card alisaie = new AlisaieLeveilleur();
        harness.setLibrary(player2, List.of(alisaie));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new AlphinaudLeveilleur());
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(alisaie);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The controller can target themselves and fail to find Alisaie")
    void controllerCanFailToFindPartner() {
        Card alisaie = new AlisaieLeveilleur();
        Card other = new GratefulApparition();
        harness.setLibrary(player1, List.of(alisaie, other));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new AlphinaudLeveilleur());
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(alisaie, other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Partner search completes when the named card is absent")
    void partnerSearchWithNoMatchingCard() {
        Card other = new GratefulApparition();
        harness.setLibrary(player2, List.of(other));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new AlphinaudLeveilleur());
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("The second spell counts a spell cast before Alphinaud entered")
    void countsSpellsCastBeforeEntering() {
        Card drawn = new GratefulApparition();
        harness.setHand(player1, List.of(new GratefulApparition(), new GratefulApparition()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.addToBattlefield(player1, new AlphinaudLeveilleur());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's second spell does not draw for Alphinaud's controller")
    void opponentsSpellsDoNotTriggerDraw() {
        Card drawn = new GratefulApparition();
        harness.addToBattlefield(player1, new AlphinaudLeveilleur());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(new GratefulApparition(), new GratefulApparition()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.castCreature(player2, 0);
        resolveAllTriggers();
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("The second-spell draw resets on the next turn")
    void drawTriggersAgainOnNextTurn() {
        harness.addToBattlefield(player1, new AlphinaudLeveilleur());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GratefulApparition(), new GratefulApparition()));
        harness.setHand(player1, List.of(new GratefulApparition(), new GratefulApparition()));
        harness.setLibrary(player1, List.of(new GratefulApparition(), new GratefulApparition(),
                new GratefulApparition(), new GratefulApparition()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
