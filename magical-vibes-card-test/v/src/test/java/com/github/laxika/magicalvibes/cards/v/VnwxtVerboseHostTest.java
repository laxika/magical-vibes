package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FloodTheEngine;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VnwxtVerboseHost.class, Forest.class, GrizzlyBears.class, Island.class,
        Mountain.class, Peek.class, Plains.class, FloodTheEngine.class})
class VnwxtVerboseHostTest extends BaseCardTest {

    @Test
    void startsEnginesAndIncreasesSpeedOnlyOncePerTurn() {
        addCreatureReady(player1, new VnwxtVerboseHost());
        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1);
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1);
        });

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void doesNotDoubleDrawBeforeMaxSpeed() {
        harness.addToBattlefield(player1, new VnwxtVerboseHost());
        gd.playerSpeeds.put(player1.getId(), 1);
        drawWithPeek();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void doublesDrawAtMaxSpeed() {
        harness.addToBattlefield(player1, new VnwxtVerboseHost());
        gd.playerSpeeds.put(player1.getId(), 4);
        drawWithPeek();

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void controllerHasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new VnwxtVerboseHost());

        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Mountain(),
                new Plains(), new Plains(), new Plains()
        ));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    private void drawWithPeek() {
        harness.setLibrary(player1, List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        ));
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
    }

    @Test
    void doublesTheNormalDrawStepDrawAtMaxSpeed() {
        harness.addToBattlefield(player1, new VnwxtVerboseHost());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        gd.turnNumber = 2;
        advanceToUpkeep(player1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void doublesEveryCardOfAMultipleCardDraw() {
        harness.addToBattlefield(player1, new VnwxtVerboseHost());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new Forest(), new Island(), new Mountain(), new Plains(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void doesNotDoubleAnOpponentsDraw() {
        harness.addToBattlefield(player1, new VnwxtVerboseHost());
        gd.playerSpeeds.put(player1.getId(), 4);
        gd.playerSpeeds.put(player2.getId(), 4);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Island(), new Mountain()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player2.getId(), 1));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    void losingAbilitiesRestoresTheNormalHandSizeLimit() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new VnwxtVerboseHost());
        floodHost(host);
        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Island(),
                new Island(), new Island(), new Mountain(), new Mountain(), new Plains()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
    }

    @Test
    void losingAbilitiesStopsDoublingDrawsEvenAtMaxSpeed() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new VnwxtVerboseHost());
        gd.playerSpeeds.put(player1.getId(), 4);
        floodHost(host);

        drawWithPeek();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void opponentsTurnLifeLossDoesNotIncreaseControllersSpeed() {
        harness.addToBattlefield(player1, new VnwxtVerboseHost());
        harness.forceActivePlayer(player2);
        harness.runStateBasedActions();

        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1));

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDoubleDrawAtSpeedThree() {
        harness.addToBattlefield(player1, new VnwxtVerboseHost());
        gd.playerSpeeds.put(player1.getId(), 3);

        drawWithPeek();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotRemoveOpponentsHandSizeLimit() {
        harness.addToBattlefield(player1, new VnwxtVerboseHost());
        harness.setHand(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Island(),
                new Island(), new Island(), new Mountain(), new Mountain(), new Plains()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
    }
    private void floodHost(Permanent host) {
        harness.setHand(player1, List.of(new FloodTheEngine()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, host.getId());
        resolveAllTriggers();
    }
}
