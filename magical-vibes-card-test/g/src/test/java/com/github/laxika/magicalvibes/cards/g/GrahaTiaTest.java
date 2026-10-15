package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
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

@CardUsed({GrahaTia.class, AngelsFeather.class, Forest.class, GrizzlyBears.class, WrathOfGod.class,
        TreetopVillage.class})
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
    @DisplayName("Triggers when it dies alongside another qualifying creature")
    void triggersWhenItDiesAlongsideAnotherCreature() {
        harness.addToBattlefield(player1, new GrahaTia());
        harness.addToBattlefield(player1, new GrizzlyBears());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0);
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

    @Test
    @DisplayName("An irrelevant death does not consume the once-per-turn trigger")
    void irrelevantDeathsDoNotPreventLaterDraw() {
        harness.addToBattlefield(player1, new GrahaTia());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        putIntoGraveyard(opponentCreature);
        putIntoGraveyard(land);
        putIntoGraveyard(ownCreature);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("A second death before the first trigger resolves does not trigger again")
    void triggersOnlyOnceBeforeResolution() {
        harness.addToBattlefield(player1, new GrahaTia());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        seedLibrary(2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, artifact));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Draws when an animated land you control dies")
    void drawsWhenAnimatedLandDies() {
        Permanent village = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new GrahaTia());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        putIntoGraveyard(village);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    private void seedLibrary(int count) {
        harness.setLibrary(player1, java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new Forest()).toList());
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
