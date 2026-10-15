package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SparringRegimen.class, EnvironmentalSciences.class, EagerFirstYear.class})
class SparringRegimenTest extends BaseCardTest {

    @Test
    @DisplayName("ETB Learn searches for a Lesson after declining to discard")
    void etbLearnSearchesForLesson() {
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new EagerFirstYear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));

        castRegimen(new EagerFirstYear());

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
    @DisplayName("ETB Learn discards and draws when the discard branch is accepted")
    void etbLearnDiscardsAndDraws() {
        Card discarded = new EagerFirstYear();
        Card drawn = new EnvironmentalSciences();
        harness.setLibrary(player1, List.of(drawn));

        castRegimen(discarded);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Attacking puts a counter on and untaps the target attacking creature")
    void attackingPutsCounterOnAndUntapsTarget() {
        harness.addToBattlefield(player1, new SparringRegimen());
        Permanent attacker = addCreatureReady(player1, new EagerFirstYear());

        declareAttackers(player1, List.of(1));

        assertThat(attacker.isTapped()).isTrue();
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        harness.addToBattlefield(player1, new SparringRegimen());
        addCreatureReady(player1, new EagerFirstYear());
        Permanent nonAttacker = addCreatureReady(player1, new EagerFirstYear());

        declareAttackers(player1, List.of(1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void learnCanDeclineBothChoices() {
        Card lesson = new EnvironmentalSciences();
        Card retained = new EagerFirstYear();
        Card topCard = new EagerFirstYear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setLibrary(player1, List.of(topCard));

        castRegimen(retained);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void learnWithEmptyHandCanTakeLesson() {
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castRegimen();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
    }

    @Test
    void learnWithNoHandOrLessonFinishesWithoutDrawing() {
        Card topCard = new EagerFirstYear();
        harness.setLibrary(player1, List.of(topCard));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>());

        castRegimen();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleAttackersProduceOnlyOneCounterOnChosenAttacker() {
        harness.addToBattlefield(player1, new SparringRegimen());
        Permanent first = addCreatureReady(player1, new EagerFirstYear());
        Permanent second = addCreatureReady(player1, new EagerFirstYear());

        declareAttackers(player1, List.of(1, 2));
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentAttackingDoesNotTriggerRegimen() {
        harness.addToBattlefield(player1, new SparringRegimen());
        Permanent attacker = addCreatureReady(player2, new EagerFirstYear());

        declareAttackers(player2, List.of(0));

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetRemovedFromCombatBeforeResolutionGetsNeitherCounterNorUntap() {
        harness.addToBattlefield(player1, new SparringRegimen());
        Permanent attacker = addCreatureReady(player1, new EagerFirstYear());

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void castRegimen(Card... additionalHandCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new SparringRegimen());
        hand.addAll(List.of(additionalHandCards));
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
