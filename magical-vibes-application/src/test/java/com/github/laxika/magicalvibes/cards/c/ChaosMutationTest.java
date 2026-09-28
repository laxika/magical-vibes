package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaosMutation.class, FountainOfYouth.class, GrizzlyBears.class, LlanowarElves.class})
class ChaosMutationTest extends BaseCardTest {

    @Test
    void exilesTargetsAndReplacesEachFromItsControllersLibrary() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        FountainOfYouth firstBottomCard = new FountainOfYouth();
        FountainOfYouth secondBottomCard = new FountainOfYouth();
        harness.setLibrary(player1, List.of(firstBottomCard, new LlanowarElves()));
        harness.setLibrary(player2, List.of(secondBottomCard, new GrizzlyBears()));
        castChaosMutation(List.of(bear.getId(), elves.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bear.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(elves.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstBottomCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondBottomCard);
    }

    @Test
    void rejectsTwoCreaturesControlledByTheSamePlayer() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    void rejectsNonCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void mayBeCastWithNoTargets() {
        prepareCard();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void castChaosMutation(List<UUID> targetIds) {
        prepareCard();
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new ChaosMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
