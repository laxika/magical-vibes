package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.q.QuandrixCampus;
import com.github.laxika.magicalvibes.cards.s.ScurridColony;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ArcaneSubtraction.class, EnvironmentalSciences.class, QuandrixCampus.class, ScurridColony.class})
class ArcaneSubtractionTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -4/-0 until end of turn")
    void givesTargetCreatureNegativePower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScurridColony());

        castArcaneSubtraction(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Learn finds a Lesson after declining to discard")
    void learnSearchesForLesson() {
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new ScurridColony();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScurridColony());
        Card cardToKeep = new QuandrixCampus();

        castArcaneSubtraction(creature.getId(), cardToKeep);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson, cardToKeep);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
    }

    @Test
    @DisplayName("Learn discards a card and draws a card when accepted")
    void learnDiscardsAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScurridColony());
        Card discarded = new ScurridColony();
        Card drawn = new QuandrixCampus();
        harness.setLibrary(player1, List.of(drawn));

        castArcaneSubtraction(creature.getId(), discarded);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new QuandrixCampus());
        harness.setHand(player1, List.of(new ArcaneSubtraction()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Learn can take a Lesson with an empty hand")
    void learnsWithEmptyHand() {
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScurridColony());

        castArcaneSubtraction(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Learn may decline both discarding and taking a Lesson")
    void mayDeclineLearning() {
        Card lesson = new EnvironmentalSciences();
        Card kept = new QuandrixCampus();
        Card undrawn = new ScurridColony();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setLibrary(player1, List.of(undrawn));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScurridColony());

        castArcaneSubtraction(creature.getId(), kept);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-2);
    }

    @Test
    @DisplayName("An illegal target prevents the entire spell from resolving, including learn")
    void doesNotLearnWhenTargetLeavesBattlefield() {
        Card lesson = new EnvironmentalSciences();
        Card kept = new QuandrixCampus();
        ArcaneSubtraction spell = new ArcaneSubtraction();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScurridColony());
        harness.setHand(player1, List.of(spell, kept));
        addMana();
        harness.castInstant(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setGraveyard(player2, List.of(creature.getCard()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }

    private void castArcaneSubtraction(java.util.UUID target, Card... additionalHandCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new ArcaneSubtraction());
        hand.addAll(List.of(additionalHandCards));
        harness.setHand(player1, hand);
        addMana();
        harness.castAndResolveInstant(player1, 0, target);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
