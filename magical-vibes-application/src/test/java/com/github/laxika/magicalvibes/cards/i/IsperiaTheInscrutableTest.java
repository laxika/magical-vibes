package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.s.SimicInitiate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IsperiaTheInscrutable.class, MistralCharger.class, SimicInitiate.class})
class IsperiaTheInscrutableTest extends BaseCardTest {

    @Test
    @DisplayName("A matching name reveals the hand and searches for a creature with flying")
    void matchingNameSearchesForFlyingCreature() {
        Card flyingCreature = new MistralCharger();
        Card nonFlyingCreature = new SimicInitiate();
        harness.setLibrary(player1, List.of(flyingCreature, nonFlyingCreature));
        Card revealedCard = new SimicInitiate();
        harness.setHand(player2, List.of(revealedCard));
        dealCombatDamageWithIsperia();

        harness.handleListChoice(player1, "Simic Initiate");

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(flyingCreature);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(flyingCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonFlyingCreature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealedCard);
    }

    @Test
    @DisplayName("A name present only in the controller's hand does not search")
    void nameAbsentFromDamagedPlayersHandDoesNotSearch() {
        Card flyingCreature = new MistralCharger();
        harness.setLibrary(player1, List.of(flyingCreature));
        Card controllerHandCard = new MistralCharger();
        Card damagedPlayerHandCard = new SimicInitiate();
        harness.setHand(player1, List.of(controllerHandCard));
        harness.setHand(player2, List.of(damagedPlayerHandCard));
        dealCombatDamageWithIsperia();

        harness.handleListChoice(player1, "Mistral Charger");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(flyingCreature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerHandCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(damagedPlayerHandCard);
    }

    @Test
    @DisplayName("A real card name can be chosen even when that card is absent from the game")
    void canNameCardAbsentFromGame() {
        Card flyingCreature = new MistralCharger();
        harness.setLibrary(player1, List.of(flyingCreature));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new SimicInitiate()));
        dealCombatDamageWithIsperia();

        harness.handleListChoice(player1, "Azorius First-Wing");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(flyingCreature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty revealed hand does not cause a library search")
    void emptyHandDoesNotSearch() {
        Card flyingCreature = new MistralCharger();
        harness.setLibrary(player1, List.of(flyingCreature));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        dealCombatDamageWithIsperia();

        harness.handleListChoice(player1, "Mistral Charger");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(flyingCreature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A matching name permits failing to find even with a flying creature in the library")
    void mayFailToFindFlyingCreature() {
        Card flyingCreature = new MistralCharger();
        harness.setLibrary(player1, List.of(flyingCreature));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new SimicInitiate()));
        dealCombatDamageWithIsperia();

        harness.handleListChoice(player1, "Simic Initiate");
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(flyingCreature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    @DisplayName("A matching name with no flying creature still completes the search and shuffles")
    void noEligibleCreatureStillShuffles() {
        Card nonFlyingCreature = new SimicInitiate();
        harness.setLibrary(player1, List.of(nonFlyingCreature));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new SimicInitiate()));
        dealCombatDamageWithIsperia();

        harness.handleListChoice(player1, "Simic Initiate");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonFlyingCreature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    private void dealCombatDamageWithIsperia() {
        addCreatureReady(player1, new IsperiaTheInscrutable());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOf(ChoiceContext.ChooseCardNameRevealHandThenChoice.class);
    }
}
