package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IgneousInspiration.class, EnvironmentalSciences.class, EagerFirstYear.class})
class IgneousInspirationTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage and searches for a Lesson after declining to discard")
    void dealsDamageAndFindsLesson() {
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new EagerFirstYear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));

        castIgneousInspiration(player2.getId(), new EagerFirstYear());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
    }

    @Test
    @DisplayName("Deals 3 damage and discards and draws when Learn is accepted")
    void dealsDamageAndDiscardsAndDraws() {
        Card discarded = new EagerFirstYear();
        Card drawn = new EagerFirstYear();
        harness.setLibrary(player1, List.of(drawn));

        castIgneousInspiration(player2.getId(), discarded);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Searches directly for a Lesson when the hand is empty")
    void searchesForLessonWithEmptyHand() {
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castIgneousInspiration(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
    }

    @Test
    @DisplayName("Can decline both Learn choices")
    void canDeclineBothChoices() {
        Card retained = new EagerFirstYear();
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castIgneousInspiration(player2.getId(), retained);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals lethal damage to a creature and still learns")
    void killsCreatureAndLearns() {
        Card creature = new EagerFirstYear();
        var target = harness.addToBattlefieldAndReturn(player2, creature);
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castIgneousInspiration(target.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not learn when its only target leaves the battlefield")
    void doesNotLearnWithIllegalTarget() {
        Card creature = new EagerFirstYear();
        var target = harness.addToBattlefieldAndReturn(player2, creature);
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(new IgneousInspiration()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(creature);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Igneous Inspiration");
    }

    @Test
    @DisplayName("Learn finishes without drawing when there are no available choices")
    void noChoicesDoesNotDraw() {
        Card undrawn = new EagerFirstYear();
        harness.setLibrary(player1, List.of(undrawn));

        castIgneousInspiration(player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 17);
    }

    private void castIgneousInspiration(UUID targetId, Card... additionalHandCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new IgneousInspiration());
        hand.addAll(List.of(additionalHandCards));
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
