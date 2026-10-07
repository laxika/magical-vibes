package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheElderDragonWar.class, GrizzlyBears.class})
class TheElderDragonWarTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I deals 2 damage to each creature and each opponent")
    void chapterIDamagesCreaturesAndOpponent() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Chapter II lets only the controller discard and draw")
    void chapterIIDiscardsAndDrawsForControllerOnly() {
        addSagaWithLore(1);
        GrizzlyBears player1Discard = new GrizzlyBears();
        GrizzlyBears player1Keep = new GrizzlyBears();
        GrizzlyBears player2Discard = new GrizzlyBears();
        harness.setHand(player1, List.of(player1Discard, player1Keep));
        harness.setHand(player2, List.of(player2Discard));
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Discard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(player2Discard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(player1Keep, drawn);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Discard);
    }

    @Test
    @DisplayName("Chapter III creates a flying 4/4 red Dragon token")
    void chapterIIICreatesDragonToken() {
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Read ahead can skip directly to chapter III")
    void readAheadStartsAtChapterThree() {
        harness.castFromHand(player1, new TheElderDragonWar(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "3");
        harness.assertOnBattlefield(player1, "The Elder Dragon War");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        harness.assertNotOnBattlefield(player1, "The Elder Dragon War");
        harness.assertInGraveyard(player1, "The Elder Dragon War");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chapter II permits discarding zero without involving the opponent")
    void chapterIICanDiscardZero() {
        addSagaWithLore(1);
        TheElderDragonWar retained = new TheElderDragonWar();
        TheElderDragonWar opponentRetained = new TheElderDragonWar();
        TheElderDragonWar undrawn = new TheElderDragonWar();
        harness.setHand(player1, List.of(retained));
        harness.setHand(player2, List.of(opponentRetained));
        harness.setLibrary(player1, List.of(undrawn));

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentRetained);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheElderDragonWar());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
    }
}
