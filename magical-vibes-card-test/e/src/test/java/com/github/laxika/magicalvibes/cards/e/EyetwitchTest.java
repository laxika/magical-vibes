package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Eyetwitch.class, EnvironmentalSciences.class, Forest.class, GrizzlyBears.class, Shock.class})
class EyetwitchTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, Learn searches for a Lesson after declining to discard")
    void deathLearnSearchesForLesson() {
        Permanent eyetwitch = harness.addToBattlefieldAndReturn(player1, new Eyetwitch());
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new GrizzlyBears();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));

        castShockAt(eyetwitch);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(eyetwitch.getId()));
        harness.assertInGraveyard(player1, "Eyetwitch");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
    }

    @Test
    @DisplayName("When it dies, Learn discards and draws when its controller has a card in hand")
    void deathLearnDiscardsAndDraws() {
        Permanent eyetwitch = harness.addToBattlefieldAndReturn(player1, new Eyetwitch());
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        castShockAt(eyetwitch);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("When it dies with an empty hand, Learn searches directly for a Lesson")
    void deathLearnSearchesWithEmptyHand() {
        Permanent eyetwitch = harness.addToBattlefieldAndReturn(player1, new Eyetwitch());
        Card lesson = new EnvironmentalSciences();
        harness.setHand(player1, List.of());
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castShockAt(eyetwitch);
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
    }

    @Test
    @DisplayName("Learn allows declining both discard and an available Lesson")
    void deathLearnCanDoNothing() {
        Permanent eyetwitch = harness.addToBattlefieldAndReturn(player1, new Eyetwitch());
        Card retained = new Eyetwitch();
        Card lesson = new EnvironmentalSciences();
        Card topCard = new Eyetwitch();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(topCard));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castShockAt(eyetwitch);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Learn with an empty hand and no Lesson completes without drawing a card")
    void deathLearnWithNoAvailableAction() {
        Permanent eyetwitch = harness.addToBattlefieldAndReturn(player1, new Eyetwitch());
        Card nonLesson = new Eyetwitch();
        Card topCard = new Eyetwitch();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(nonLesson)));

        castShockAt(eyetwitch);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Eyetwitch");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castShockAt(Permanent target) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }
}
