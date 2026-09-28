package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReedRichardsSmartestMan.class, Forest.class, GrizzlyBears.class, Island.class, Python.class})
class ReedRichardsSmartestManTest extends BaseCardTest {

    @Test
    void hasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new ReedRichardsSmartestMan());
        harness.setHand(player1, new ArrayList<>(List.of(
                new Python(), new Python(), new Python(), new Python(), new Python(),
                new Python(), new Python(), new Python(), new Python())));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void replacesOnlyTheFirstEligibleDrawEachTurnWithFourCards() {
        harness.addToBattlefield(player1, new ReedRichardsSmartestMan());
        harness.setLibrary(player1, List.of(
                new Forest(), new GrizzlyBears(), new Island(), new Forest(), new GrizzlyBears()));
        harness.setHand(player1, new ArrayList<>());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotReplaceTheFirstDrawOfItsControllersDrawStep() {
        harness.addToBattlefield(player1, new ReedRichardsSmartestMan());
        harness.setLibrary(player1, List.of(
                new Forest(), new GrizzlyBears(), new Island(), new Forest(), new GrizzlyBears()));
        harness.setHand(player1, new ArrayList<>());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotReplaceAnOpponentsDraw() {
        harness.addToBattlefield(player1, new ReedRichardsSmartestMan());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player2, new ArrayList<>());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
