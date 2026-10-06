package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Aethersnatch;
import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.h.HeatedDebate;
import com.github.laxika.magicalvibes.cards.p.ProfessorOfSymbology;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RetrieverPhoenix.class, EnvironmentalSciences.class, ProfessorOfSymbology.class,
        HeatedDebate.class, Aethersnatch.class})
class RetrieverPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Learn searches outside the game when Retriever Phoenix is cast")
    void castTriggersLearn() {
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        RetrieverPhoenix phoenix = new RetrieverPhoenix();
        harness.castFromHand(player1, phoenix, "{3}{R}");
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
    }

    @Test
    @DisplayName("Retriever Phoenix can return itself instead of another Learn")
    void returnsFromGraveyardInsteadOfLearning() {
        RetrieverPhoenix phoenix = new RetrieverPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        castProfessor();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenix.getId()));
    }

    @Test
    @DisplayName("Declining Retriever Phoenix's replacement continues with Learn")
    void decliningReplacementContinuesLearning() {
        RetrieverPhoenix phoenix = new RetrieverPhoenix();
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setGraveyard(player1, List.of(phoenix));
        castProfessor();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(phoenix);
    }

    private void castProfessor() {
        harness.castFromHand(player1, new ProfessorOfSymbology(), "{1}{W}");
        resolveAllTriggers();
    }

    @Test
    void enteringWithoutBeingCastDoesNotLearn() {
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        harness.enterBattlefieldAndReturn(player1, new RetrieverPhoenix());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
    }

    @Test
    void oneLearnReturnsOnlyOnePhoenixAndDoesNotTakeALesson() {
        RetrieverPhoenix first = new RetrieverPhoenix();
        RetrieverPhoenix second = new RetrieverPhoenix();
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setGraveyard(player1, List.of(first, second));

        castProfessor();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(second.getId()));
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsPhoenixCannotReplaceYourLearn() {
        RetrieverPhoenix phoenix = new RetrieverPhoenix();
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setGraveyard(player2, List.of(phoenix));

        castProfessor();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(phoenix);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
    }

    @Test
    void phoenixDiscardedWhileLearningCannotReplaceThatLearn() {
        RetrieverPhoenix discarded = new RetrieverPhoenix();
        Card drawn = new EnvironmentalSciences();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new ProfessorOfSymbology(), discarded));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertNotOnBattlefield(player1, "Retriever Phoenix");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void phoenixCanReturnUsingItsOwnTriggerAfterDying() {
        RetrieverPhoenix phoenix = new RetrieverPhoenix();
        harness.castFromHand(player1, phoenix, "{3}{R}");
        harness.passBothPriorities();
        var permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(phoenix.getId())).findFirst().orElseThrow();
        harness.setHand(player1, List.of(new HeatedDebate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, permanent.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(phoenix);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Retriever Phoenix");
        harness.assertNotInGraveyard(player1, "Retriever Phoenix");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void gainingControlOfPhoenixSpellDoesNotCountAsCastingIt() {
        RetrieverPhoenix phoenix = new RetrieverPhoenix();
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(lesson)));
        harness.castFromHand(player1, phoenix, "{3}{R}");
        harness.setHand(player2, List.of(new Aethersnatch()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, phoenix.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Retriever Phoenix");
        harness.assertNotOnBattlefield(player1, "Retriever Phoenix");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(lesson);
    }
}
