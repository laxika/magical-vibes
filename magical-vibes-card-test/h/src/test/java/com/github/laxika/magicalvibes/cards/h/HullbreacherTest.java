package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hullbreacher.class, Humble.class, Forest.class, GrizzlyBears.class, Island.class, Peek.class})
class HullbreacherTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces an opponent's extra draw with a Treasure token")
    void replacesOpponentExtraDrawWithTreasure() {
        harness.addToBattlefield(player1, new Hullbreacher());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertNotInHand(player2, "Forest");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A face-down Hullbreacher does not replace an opponent's extra draw")
    void faceDownHullbreacherDoesNotReplaceDraw() {
        var hullbreacher = harness.addToBattlefieldAndReturn(player1, new Hullbreacher());
        hullbreacher.setFaceDown(2, 2, java.util.Set.of(com.github.laxika.magicalvibes.model.CardType.CREATURE));
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertInHand(player2, "Forest");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Does not replace the first draw during an opponent's draw step")
    void doesNotReplaceFirstDrawStepDraw() {
        harness.addToBattlefield(player1, new Hullbreacher());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));

        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Does not replace the controller's own draw")
    void doesNotReplaceControllerDraw() {
        harness.addToBattlefield(player1, new Hullbreacher());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInHand(player1, "Island");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void replacesAdditionalDrawDuringOpponentsDrawStep() {
        harness.addToBattlefield(player1, new Hullbreacher());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));
        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.DRAW, () -> {
            harness.passBothPriorities();
            harness.setHand(player2, List.of(new Peek()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.castAndResolveInstant(player2, 0, player1.getId());
        });

        assertThat(gd.currentStep).isEqualTo(TurnStep.DRAW);
        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void stillCreatesTreasureWhenOpponentsLibraryIsEmpty() {
        harness.addToBattlefield(player1, new Hullbreacher());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void multipleHullbreachersCreateOnlyOneTreasurePerDraw() {
        harness.addToBattlefield(player1, new Hullbreacher());
        harness.addToBattlefield(player1, new Hullbreacher());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void doesNotReplaceDrawAfterLosingAbilities() {
        var hullbreacher = harness.addToBattlefieldAndReturn(player1, new Hullbreacher());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, hullbreacher.getId());

        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertInHand(player2, "Forest");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void canBeCastWithFlashBeforeOpponentsDrawSpellResolves() {
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, player1.getId());

        harness.setHand(player1, List.of(new Hullbreacher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hullbreacher");
        harness.assertNotInHand(player2, "Forest");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("In a three-player game a Hullbreacher of the opponent in the third seat replaces the extra draw")
    void replacesDrawForThirdSeatOpponent() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player3, new Hullbreacher());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player3.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Treasure"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Treasure"));
    }

    private Player addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        Player player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
        return player3;
    }

}
