package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DivideByZero.class, EnvironmentalSciences.class, Forest.class, GiantGrowth.class,
        GrizzlyBears.class, LetterOfAcceptance.class})
class DivideByZeroTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a noncreature permanent with mana value 1 or greater")
    void returnsPermanentWithSufficientManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LetterOfAcceptance());
        castDivideByZero(target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Returns a spell with mana value 1 or greater")
    void returnsSpellWithSufficientManaValue() {
        GiantGrowth growth = new GiantGrowth();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(growth));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new DivideByZero()));
        addMana();
        harness.castAndResolveInstant(player1, 0, growth.getId());

        assertThat(gd.playerHands.get(player2.getId())).contains(growth);
    }

    @Test
    @DisplayName("Cannot target a permanent with mana value 0")
    void cannotTargetZeroManaValuePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DivideByZero()));
        addMana();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Learn searches for a Lesson after declining to discard")
    void learnSearchesForLesson() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new GrizzlyBears();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));
        castDivideByZero(target.getId(), new Forest());
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
    @DisplayName("Learn discards a card and draws a card when accepted")
    void learnDiscardsAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        castDivideByZero(target.getId(), discarded);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Learn may take a Lesson when the hand is empty")
    void learnsWithEmptyHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LetterOfAcceptance());
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castDivideByZero(target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Learn may decline both discarding and taking a Lesson")
    void mayDeclineLearning() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LetterOfAcceptance());
        Card lesson = new EnvironmentalSciences();
        Card retained = new Forest();
        Card topCard = new Forest();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setLibrary(player1, List.of(topCard));

        castDivideByZero(target.getId(), retained);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does not learn when its only target leaves before resolution")
    void doesNotLearnWithIllegalTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LetterOfAcceptance());
        Card lesson = new EnvironmentalSciences();
        Card retained = new Forest();
        DivideByZero spell = new DivideByZero();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(spell, retained));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    @DisplayName("Can return its controller's own permanent and discard it to learn")
    void canReturnOwnPermanentAndDiscardIt() {
        Card returned = new LetterOfAcceptance();
        Permanent target = harness.addToBattlefieldAndReturn(player1, returned);
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        castDivideByZero(target.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(returned);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returned);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    private void castDivideByZero(java.util.UUID target, Card... additionalHandCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new DivideByZero());
        hand.addAll(List.of(additionalHandCards));
        harness.setHand(player1, hand);
        addMana();
        harness.castAndResolveInstant(player1, 0, target);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
