package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeutralizeTheGuards.class, GrizzlyBears.class})
class NeutralizeTheGuardsTest extends BaseCardTest {

    @Test
    @DisplayName("Weakens the target opponent's creatures and surveils 2")
    void weakensTargetOpponentsCreaturesAndSurveilsTwo() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();

        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new NeutralizeTheGuards()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard, secondCard);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new NeutralizeTheGuards()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Affects creatures present at resolution, but not creatures entering later")
    void affectsOnlyCreaturesPresentAtResolution() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new NeutralizeTheGuards()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, player2.getId());

        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent anotherCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();

        Permanent afterResolution = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, anotherCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, anotherCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Surveils without opposing creatures and can keep both cards in either order")
    void surveilsWithoutOpposingCreaturesAndReordersKeptCards() {
        Card first = new NeutralizeTheGuards();
        Card second = new NeutralizeTheGuards();
        Card third = new NeutralizeTheGuards();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new NeutralizeTheGuards()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second, third);
    }

    @Test
    @DisplayName("Surveil can put one card in the graveyard and keep the other above the untouched library")
    void splitsSurveilledCardsBetweenLibraryAndGraveyard() {
        Card first = new NeutralizeTheGuards();
        Card second = new NeutralizeTheGuards();
        Card third = new NeutralizeTheGuards();
        Card opponentsCard = new NeutralizeTheGuards();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setLibrary(player2, List.of(opponentsCard));
        harness.setHand(player1, List.of(new NeutralizeTheGuards()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentsCard);
    }

    @Test
    @DisplayName("Surveil 2 handles a library containing only one card")
    void surveilsOnlyAvailableCardInShortLibrary() {
        Card onlyCard = new NeutralizeTheGuards();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new NeutralizeTheGuards()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
    }

    @Test
    @DisplayName("Multiple copies stack and creatures with zero toughness die")
    void multipleCopiesCanReduceToughnessToZero() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player2, creature);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new NeutralizeTheGuards(), new NeutralizeTheGuards()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature);
    }
}
