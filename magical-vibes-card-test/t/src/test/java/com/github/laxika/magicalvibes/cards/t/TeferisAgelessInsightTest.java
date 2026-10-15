package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.r.RiverSong;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeferisAgelessInsight.class, Forest.class, GrizzlyBears.class, Island.class, Peek.class,
        RiverSong.class})
class TeferisAgelessInsightTest extends BaseCardTest {

    @Test
    @DisplayName("A draw outside the controller's draw step draws two cards instead")
    void doublesNonDrawStepDraw() {
        harness.addToBattlefield(player1, new TeferisAgelessInsight());
        harness.setLibrary(player1, List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        ));
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The first draw in the controller's own draw step is not doubled, later ones are")
    void exemptsFirstDrawStepDraw() {
        harness.addToBattlefield(player1, new TeferisAgelessInsight());
        harness.setLibrary(player1, List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island(),
                new Forest()
        ));
        harness.forceStep(TurnStep.DRAW);
        gd.activePlayerId = player1.getId();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A draw during the opponent's draw step is doubled for the controller")
    void doublesDrawInOpponentsDrawStep() {
        harness.addToBattlefield(player1, new TeferisAgelessInsight());
        harness.setLibrary(player1, List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        ));
        harness.forceStep(TurnStep.DRAW);
        gd.activePlayerId = player2.getId();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The opponent's draw is unaffected")
    void doesNotAffectOpponent() {
        harness.addToBattlefield(player1, new TeferisAgelessInsight());
        harness.setLibrary(player2, List.of(
                new Forest(),
                new GrizzlyBears()
        ));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each card of a multi-card draw is replaced separately")
    void doublesEveryCardOfMultiCardDraw() {
        harness.addToBattlefield(player1, new TeferisAgelessInsight());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Island(), new Island(), new Island(), new Forest()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 3));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A multi-card draw in the controller's draw step exempts only its first card")
    void exemptsOnlyFirstCardOfDrawStepMultiCardDraw() {
        harness.addToBattlefield(player1, new TeferisAgelessInsight());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Island(), new Island(), new Island()));
        harness.forceStep(TurnStep.DRAW);
        gd.activePlayerId = player1.getId();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 3));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Doubled draws still come from the bottom when River Song is controlled")
    void doubledDrawsRespectRiverSong() {
        harness.addToBattlefield(player1, new TeferisAgelessInsight());
        harness.addToBattlefield(player1, new RiverSong());
        harness.setHand(player1, List.of());
        Forest top = new Forest();
        Island middle = new Island();
        Forest bottom = new Forest();
        harness.setLibrary(player1, List.of(top, middle, bottom));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bottom, middle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }
}
