package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrahaTia.class, AngelsFeather.class, Forest.class, GrizzlyBears.class})
class GrahaTiaTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when another creature you control dies")
    void drawsWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new GrahaTia());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        putIntoGraveyard(creature);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Draws a card when another artifact you control dies")
    void drawsWhenOwnArtifactDies() {
        harness.addToBattlefield(player1, new GrahaTia());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        putIntoGraveyard(artifact);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Triggers only once each turn for qualifying permanents")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new GrahaTia());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        seedLibrary(2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        putIntoGraveyard(creature);
        putIntoGraveyard(artifact);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Ignores permanents not controlled by its controller and noncreature nonartifacts")
    void ignoresOpponentPermanentsAndOtherTypes() {
        harness.addToBattlefield(player1, new GrahaTia());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        putIntoGraveyard(opponentCreature);
        putIntoGraveyard(land);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
    }

    @Test
    @DisplayName("Triggers for a creature you control even when it is owned by an opponent")
    void triggersForStolenCreatureYouControl() {
        harness.addToBattlefield(player1, new GrahaTia());
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).remove(stolenCreature);
        gd.playerBattlefields.get(player1.getId()).add(stolenCreature);
        gd.stolenCreatures.put(stolenCreature.getId(), player2.getId());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        putIntoGraveyard(stolenCreature);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Triggers again on a later turn")
    void triggersAgainNextTurn() {
        harness.addToBattlefield(player1, new GrahaTia());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        seedLibrary(2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        putIntoGraveyard(firstCreature);
        advanceTurn();
        advanceTurn();

        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        putIntoGraveyard(secondCreature);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
    }

    @Test
    @CardUsed(WrathOfGod.class)
    @DisplayName("Triggers when it dies alongside another qualifying creature")
    void triggersWhenItDiesAlongsideAnotherCreature() {
        harness.addToBattlefield(player1, new GrahaTia());
        harness.addToBattlefield(player1, new GrizzlyBears());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.getGameService().playCard(gd, player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger when only G'raha Tia dies")
    void doesNotTriggerWhenOnlySelfDies() {
        Permanent grahaTia = harness.addToBattlefieldAndReturn(player1, new GrahaTia());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        putIntoGraveyard(grahaTia);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    private void seedLibrary(int count) {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
