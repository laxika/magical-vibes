package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.l.LetterOfAcceptance;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StudyBreak.class, StoneriseSpirit.class, EnvironmentalSciences.class, LetterOfAcceptance.class})
class StudyBreakTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two creatures and finds a Lesson after declining to discard")
    void tapsTwoCreaturesAndFindsLesson() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new StoneriseSpirit());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new StoneriseSpirit());
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new StoneriseSpirit();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));
        castStudyBreak(List.of(first.getId(), second.getId()), new StoneriseSpirit());

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
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
    @DisplayName("Discards and draws when the discard branch of Learn is accepted")
    void discardsAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StoneriseSpirit());
        Card discarded = new StoneriseSpirit();
        Card drawn = new EnvironmentalSciences();
        harness.setLibrary(player1, List.of(drawn));
        castStudyBreak(List.of(creature.getId()), discarded);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Searches directly for a Lesson when the hand is empty")
    void searchesForLessonWithEmptyHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StoneriseSpirit());
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        castStudyBreak(List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new LetterOfAcceptance());
        harness.setHand(player1, List.of(new StudyBreak()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can learn with no targets while leaving creatures untapped")
    void learnsWithNoTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StoneriseSpirit());
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castStudyBreak(List.of());
        harness.handleCardChosen(player1, 0);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can decline both discarding and taking an available Lesson")
    void canDeclineBothLearnOptions() {
        Card kept = new StoneriseSpirit();
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        castStudyBreak(List.of(), kept);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Still taps and learns when only one of two targets remains legal")
    void resolvesWithOneRemainingTarget() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new StoneriseSpirit());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new StoneriseSpirit());
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(new StudyBreak()));
        addMana();
        harness.castInstant(player1, 0, List.of(removed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(remaining.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
    }

    @Test
    @DisplayName("Does not learn when its only target has left the battlefield")
    void doesNotLearnWhenAllTargetsAreIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StoneriseSpirit());
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(new StudyBreak()));
        addMana();
        harness.castInstant(player1, 0, List.of(creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        harness.assertInGraveyard(player1, "Study Break");
    }

    @Test
    @DisplayName("Cannot choose more than two creatures")
    void cannotChooseThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new StoneriseSpirit());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new StoneriseSpirit());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new StoneriseSpirit());
        harness.setHand(player1, List.of(new StudyBreak()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An already tapped creature remains a legal target and Learn still happens")
    void canTargetTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StoneriseSpirit());
        creature.tap();
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castStudyBreak(List.of(creature.getId()));
        harness.handleCardChosen(player1, 0);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
    }

    @Test
    @DisplayName("Learn does nothing with an empty hand and no Lessons outside the game")
    void resolvesWithoutAvailableLearnOptions() {
        gd.playerSideboards.put(player1.getId(), new ArrayList<>());
        castStudyBreak(List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Study Break");
    }

    private void castStudyBreak(List<java.util.UUID> targets, Card... additionalHandCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new StudyBreak());
        hand.addAll(List.of(additionalHandCards));
        harness.setHand(player1, hand);
        addMana();
        harness.castAndResolveInstant(player1, 0, targets);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
