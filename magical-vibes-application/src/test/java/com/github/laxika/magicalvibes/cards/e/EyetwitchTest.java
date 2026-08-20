package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EyetwitchTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, Learn searches for a Lesson after declining to discard")
    void deathLearnSearchesForLesson() {
        Permanent eyetwitch = harness.addToBattlefieldAndReturn(player1, new Eyetwitch());
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new GrizzlyBears();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));

        castShockAt(eyetwitch);
        resolveDeathAndLearn();

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
        resolveDeathAndLearn();

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
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castShockAt(eyetwitch);
        resolveDeathAndLearn();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
    }

    private void castShockAt(Permanent target) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
    }

    private void resolveDeathAndLearn() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
