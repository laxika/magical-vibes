package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({BirthOfTheImperium.class, GrizzlyBears.class})
class BirthOfTheImperiumTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates one vigilant Astartes Warrior per opponent")
    void chapterICreatesAstartesWarriors() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();

        List<Permanent> tokens = findPermanents(player1, "Astartes Warrior");
        assertThat(tokens).hasSize(1);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.ASTARTES, CardSubtype.WARRIOR);
            assertThat(token.hasKeyword(Keyword.VIGILANCE)).isTrue();
        });
    }

    @Test
    @DisplayName("Chapter II makes each opponent sacrifice a creature")
    void chapterIIMakesOpponentSacrificeCreature() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        harness.addToBattlefield(player2, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Chapter III draws two cards when you control more creatures")
    void chapterIIIDrawsWhenYouControlMoreCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Chapter III does not draw when creature counts are tied")
    void chapterIIIDoesNotDrawWhenCreatureCountsAreTied() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void chapterITriggersWhenCast() {
        harness.castFromHand(player1, new BirthOfTheImperium(), "{2}{W}{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Astartes Warrior")).hasSize(1);
        harness.assertOnBattlefield(player1, "Birth of the Imperium");
    }

    @Test
    void chapterIIDoesNothingWhenOpponentHasNoCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToNextChapter();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIILetsOpponentChooseTheirCreature() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        saga.setCounterCount(CounterType.LORE, 1);
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent kept = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToNextChapter();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kept).doesNotContain(chosen);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void chapterICreatesTwoTokensForTwoOpponents() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        saga.setCounterCount(CounterType.LORE, 0);
        triggerNextChapter();
        addThirdPlayer();

        resolveChapter();

        assertThat(findPermanents(player1, "Astartes Warrior")).hasSize(2);
    }

    @Test
    void chapterIIIChecksCreatureCountsAtResolutionAndSacrificesSagaAfterward() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        triggerNextChapter();
        harness.assertOnBattlefield(player1, "Birth of the Imperium");
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Birth of the Imperium");
        harness.assertInGraveyard(player1, "Birth of the Imperium");
    }

    @Test
    void chapterIIIDrawsFourForTwoQualifyingOpponents() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        triggerNextChapter();
        addThirdPlayer();

        resolveChapter();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void chapterIIICountsQualifyingOpponentOtherThanFirstOpponent() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        triggerNextChapter();
        addThirdPlayer();

        resolveChapter();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void chapterIIRequiresAllOpponentsToChooseBeforeSacrificing() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BirthOfTheImperium());
        saga.setCounterCount(CounterType.LORE, 1);
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent kept = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        triggerNextChapter();
        Player player3 = addThirdPlayer();
        Permanent thirdChosen = harness.addToBattlefieldAndReturn(player3, new GrizzlyBears());
        Permanent thirdKept = harness.addToBattlefieldAndReturn(player3, new GrizzlyBears());

        resolveChapter();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(chosen, kept);
        assertThat(gd.playerBattlefields.get(player3.getId())).contains(thirdChosen, thirdKept);
        harness.handleMultiplePermanentsChosen(player3, List.of(thirdChosen.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerBattlefields.get(player3.getId())).containsExactly(thirdKept);
    }

    private Player addThirdPlayer() {
        UUID id = UUID.randomUUID();
        Player player = new Player(id, "Charlie");
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
        return player;
    }

    private void resolveChapter() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }

    private void advanceToNextChapter() {
        triggerNextChapter();
        harness.passBothPriorities();
    }
}
