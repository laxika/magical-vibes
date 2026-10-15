package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.l.LetterOfAcceptance;
import com.github.laxika.magicalvibes.cards.s.ScurridColony;
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

@CardUsed({GuidingVoice.class, ScurridColony.class, EnvironmentalSciences.class, LetterOfAcceptance.class})
class GuidingVoiceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on a creature, then reveals a Lesson after declining to discard")
    void putsCounterAndFindsLesson() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new ScurridColony();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));
        castGuidingVoice(creature, new ScurridColony());

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        Card discarded = new ScurridColony();
        Card drawn = new ScurridColony();
        harness.setLibrary(player1, List.of(drawn));
        castGuidingVoice(creature, discarded);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Searches directly for a Lesson when the hand is empty")
    void searchesForLessonWithEmptyHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        castGuidingVoice(creature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
    }

    @Test
    @DisplayName("Does nothing for Learn when neither a Lesson nor a discard is available")
    void learnDoesNothingWithoutAvailableCards() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        castGuidingVoice(creature);

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Card noncreature = new LetterOfAcceptance();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, noncreature);
        harness.setHand(player1, List.of(new GuidingVoice()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can decline both discarding and taking an available Lesson")
    void canDeclineLearning() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        Card retained = new ScurridColony();
        Card lesson = new EnvironmentalSciences();
        Card libraryCard = new LetterOfAcceptance();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setLibrary(player1, List.of(libraryCard));
        castGuidingVoice(creature, retained);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty hand does not force the player to take a Lesson")
    void canDeclineLessonWithEmptyHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        castGuidingVoice(creature);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can target an opponent's creature while the caster learns")
    void targetsOpponentCreatureAndCasterLearns() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScurridColony());
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        castGuidingVoice(creature);
        harness.handleCardChosen(player1, 0);

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(lesson);
    }

    @Test
    @DisplayName("Does not learn when the sole target leaves before resolution")
    void doesNotLearnWithIllegalTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        Card lesson = new EnvironmentalSciences();
        Card retained = new ScurridColony();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(new GuidingVoice(), retained));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.setGraveyard(player1, List.of(creature.getCard()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        harness.assertInGraveyard(player1, "Guiding Voice");
    }

    private void castGuidingVoice(Permanent target, Card... additionalHandCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new GuidingVoice());
        hand.addAll(List.of(additionalHandCards));
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
