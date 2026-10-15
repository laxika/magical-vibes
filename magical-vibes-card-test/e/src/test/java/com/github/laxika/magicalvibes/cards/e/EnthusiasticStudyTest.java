package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LoreholdCampus;
import com.github.laxika.magicalvibes.cards.a.AgelessGuardian;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnthusiasticStudy.class, AgelessGuardian.class, EnvironmentalSciences.class, LoreholdCampus.class})
class EnthusiasticStudyTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +3/+1 and trample until end of turn")
    void boostsAndGrantsTrampleUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AgelessGuardian());

        castEnthusiasticStudy(target);

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Finds a Lesson after declining to discard")
    void findsLesson() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AgelessGuardian());
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new AgelessGuardian();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));
        castEnthusiasticStudy(target, new AgelessGuardian());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
    }

    @Test
    @DisplayName("Discards and draws when Learn is accepted")
    void discardsAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AgelessGuardian());
        Card discarded = new AgelessGuardian();
        Card drawn = new LoreholdCampus();
        harness.setLibrary(player1, List.of(drawn));
        castEnthusiasticStudy(target, discarded);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Searches directly for a Lesson when the hand is empty")
    void searchesForLessonWithEmptyHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AgelessGuardian());
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        castEnthusiasticStudy(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new LoreholdCampus());
        harness.setHand(player1, List.of(new EnthusiasticStudy()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("May decline both discarding and taking a Lesson")
    void mayDeclineLearn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AgelessGuardian());
        Card kept = new AgelessGuardian();
        Card lesson = new EnvironmentalSciences();
        Card topCard = new LoreholdCampus();
        harness.setLibrary(player1, List.of(topCard));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castEnthusiasticStudy(target, kept);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not learn when the only target leaves before resolution")
    void doesNotLearnWithIllegalTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AgelessGuardian());
        Card lesson = new EnvironmentalSciences();
        Card kept = new AgelessGuardian();
        EnthusiasticStudy spell = new EnthusiasticStudy();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(spell, kept));
        addMana();
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    private void castEnthusiasticStudy(Permanent target, Card... additionalHandCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new EnthusiasticStudy());
        hand.addAll(List.of(additionalHandCards));
        harness.setHand(player1, hand);
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
