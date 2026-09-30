package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    private void castWall(List<Forest> library, Card firstCreature, Card secondCreature) {
        harness.setLibrary(player1, library);
        harness.addToBattlefield(player1, firstCreature);
        harness.addToBattlefield(player1, secondCreature);
        harness.setHand(player1, List.of(new WallOfMourning()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
