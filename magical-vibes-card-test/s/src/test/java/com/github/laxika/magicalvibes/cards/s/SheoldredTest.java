package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.t.TheTrueScriptures;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sheoldred.class, TheTrueScriptures.class, GrizzlyBears.class, LilianaVess.class, Shock.class,
        SoulWarden.class})
class SheoldredTest extends BaseCardTest {

    @Test
    @DisplayName("Sheoldred makes each opponent sacrifice a nontoken creature or planeswalker")
    void etbSacrificesAnOpponentsNontokenCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Sheoldred(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sheoldred can transform only when an opponent has eight cards in their graveyard")
    void transformsWithTheGraveyardCondition() {
        Permanent sheoldred = addReadySheoldred();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.setGraveyard(player2, filler(8));
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sheoldred), null, null);
        harness.passBothPriorities();

        Permanent transformed = findPermanent(player1, "The True Scriptures");
        assertThat(transformed.isTransformed()).isTrue();
        assertThat(transformed.getCard().getName()).isEqualTo("The True Scriptures");
    }

    @Test
    @DisplayName("The True Scriptures chapter I destroys one target creature or planeswalker per opponent")
    void chapterIDestroysChosenOpponentPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        target.setCounterCount(CounterType.LOYALTY, target.getCard().getLoyalty());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent saga = addTransformedSaga();
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Liliana Vess");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The True Scriptures chapter III returns creatures and resets to Sheoldred")
    void chapterIIIReturnsCreaturesAndTheFrontFace() {
        Permanent saga = addTransformedSaga();
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        advanceToNextChapter();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Sheoldred")
                        && !permanent.isTransformed());
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("The True Scriptures"));
    }

    private Permanent addReadySheoldred() {
        Permanent permanent = addCreatureReady(player1, new Sheoldred());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    @Test
    void chapterIIDiscardsThreeBeforeMillingThree() {
        Permanent saga = addTransformedSaga();
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock(), new GrizzlyBears()));
        List<Card> library = filler(4);
        harness.setLibrary(player2, library);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.get(3));
        harness.assertInHand(player1, "Shock");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void chapterIIMillsEvenWithAnEmptyHand() {
        Permanent saga = addTransformedSaga();
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, filler(2));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void chapterIIIReanimatesCreaturesSimultaneously() {
        Permanent saga = addTransformedSaga();
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new SoulWarden(), new Shock()));
        harness.setGraveyard(player2, List.of());
        harness.setLife(player1, 20);

        advanceToNextChapter();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Soul Warden");
        harness.assertOnBattlefield(player1, "Sheoldred");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player1, 22);
    }

    @Test
    void transformConditionIsNotRecheckedOnResolution() {
        addReadySheoldred();
        harness.setGraveyard(player2, filler(8));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.setGraveyard(player2, List.of());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The True Scriptures");
        assertThat(findPermanent(player1, "The True Scriptures").getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void cannotActivateTransformOutsideSorceryTiming() {
        addReadySheoldred();
        harness.setGraveyard(player2, filler(8));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Sheoldred");
    }

    @Test
    void opponentChoosesWhichNontokenPermanentToSacrifice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        planeswalker.setCounterCount(CounterType.LOYALTY, planeswalker.getCard().getLoyalty());
        harness.castFromHand(player1, new Sheoldred(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, planeswalker.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertInGraveyard(player2, "Liliana Vess");
    }

    @Test
    void etbDoesNotSacrificeTokens() {
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);
        harness.castFromHand(player1, new Sheoldred(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterICanChooseNoTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent saga = addTransformedSaga();
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void chapterICanTargetOnePermanentForEachOpponent() {
        UUID id = UUID.randomUUID();
        Player player3 = new Player(id, "Charlie");
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), id, "Charlie");
        Permanent target2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target3 = harness.addToBattlefieldAndReturn(player3, new GrizzlyBears());
        Permanent saga = addTransformedSaga();
        saga.setCounterCount(CounterType.LORE, 0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();

        List<Player> players = List.of(player1, player2, player3);
        for (int i = 0; i < 6 && !gd.interaction.isAwaitingInput(); i++) {
            UUID priorityPlayer = gqs.getPriorityPlayerId(gd);
            harness.passPriority(players.stream().filter(p -> p.getId().equals(priorityPlayer))
                    .findFirst().orElseThrow());
        }
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player1, List.of(target2.getId(), target3.getId()));
        for (int i = 0; i < 6 && !gd.stack.isEmpty(); i++) {
            UUID priorityPlayer = gqs.getPriorityPlayerId(gd);
            harness.passPriority(players.stream().filter(p -> p.getId().equals(priorityPlayer))
                    .findFirst().orElseThrow());
        }

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player3, "Grizzly Bears");
    }

    private Permanent addTransformedSaga() {
        Sheoldred front = new Sheoldred();
        Permanent permanent = new Permanent(front);
        permanent.setCard(front.getBackFaceCard());
        permanent.setTransformed(true);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }
}
