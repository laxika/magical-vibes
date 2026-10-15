package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfMourning.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class WallOfMourningTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles one card from the top of its controller's library face down")
    void etbExilesOneCardFaceDown() {
        Forest topCard = new Forest();
        castWall(List.of(topCard), new GrizzlyBears(), new LlanowarElves());

        assertThat(gd.getCardsExiledByPermanent(harness.getPermanentId(player1, "Wall of Mourning")))
                .extracting(card -> card.getId())
                .containsExactly(topCard.getId());
        assertThat(gd.findExiledCard(topCard.getId()).faceDown()).isTrue();
    }

    @Test
    @DisplayName("Coven returns an exiled card to its owner's hand at the controller's end step")
    void covenReturnsExiledCardAtEndStep() {
        Forest topCard = new Forest();
        castWall(List.of(topCard), new GrizzlyBears(), new LlanowarElves());

        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("Coven does not trigger without three creatures with different powers")
    void covenDoesNotTriggerWithoutDifferentPowers() {
        Forest topCard = new Forest();
        castWall(List.of(topCard), new GrizzlyBears(), new GrizzlyBears());

        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    void covenIsCheckedAgainWhenTheTriggerResolves() {
        Forest topCard = new Forest();
        castWall(List.of(topCard), new GrizzlyBears(), new LlanowarElves());
        beginEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof LlanowarElves);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    void opponentEndStepDoesNotReturnAnExiledCard() {
        Forest topCard = new Forest();
        castWall(List.of(topCard), new GrizzlyBears(), new LlanowarElves());

        beginEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    void emptyLibraryDoesNotPreventWallFromEnteringOrCovenFromResolving() {
        castWall(List.of(), new GrizzlyBears(), new LlanowarElves());

        advanceToEndStep();

        assertThat(gd.getCardsExiledByPermanent(harness.getPermanentId(player1, "Wall of Mourning")))
                .isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newControllerReturnsTheExiledCardToItsOriginalOwner() {
        Forest topCard = new Forest();
        castWall(List.of(topCard), new GrizzlyBears(), new LlanowarElves());
        var wall = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof WallOfMourning)
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(wall);
        gd.playerBattlefields.get(player2.getId()).add(wall);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        beginEndStep(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    private void castWall(List<Forest> library, Card firstCreature, Card secondCreature) {
        harness.setLibrary(player1, library);
        harness.addToBattlefield(player1, firstCreature);
        harness.addToBattlefield(player1, secondCreature);
        harness.castFromHand(player1, new WallOfMourning(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        beginEndStep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void beginEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
