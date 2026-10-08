package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongOfERendil.class, Forest.class, Island.class, GrizzlyBears.class, StormCrow.class, EsixFractalBloom.class})
class SongOfERendilTest extends BaseCardTest {

    @Test
    void chapterIScriesTwoThenDrawsTwoCards() {
        addSagaWithLore(0);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island()));

        advanceToNextChapter();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void chapterIICreatesTreasureAndFlyingBird() {
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE));
        Permanent bird = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.BIRD))
                .findFirst()
                .orElseThrow();
        assertThat(bird.getCard().getPower()).isEqualTo(2);
        assertThat(bird.getCard().getToughness()).isEqualTo(2);
        assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    void chapterIIIAddsFlyingCountersOnlyToCreaturesWithoutFlying() {
        addSagaWithLore(2);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent crow = harness.addToBattlefieldAndReturn(player1, new StormCrow());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(crow.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(opponentBears.getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    void chapterIDrawsAfterPuttingBothScryCardsOnBottom() {
        addSagaWithLore(0);
        Forest first = new Forest();
        Island second = new Island();
        GrizzlyBears third = new GrizzlyBears();
        StormCrow fourth = new StormCrow();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    void chapterIICreatesBothTokensInOneEventForEsix() {
        addSagaWithLore(1);
        harness.addToBattlefield(player1, new EsixFractalBloom());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.getCard().getName())
                        .isEqualTo(bears.getCard().getName()));
    }

    @Test
    void chapterIIISkipsCreaturesWithFlyingCountersAndNoncreatures() {
        addSagaWithLore(2);
        Permanent flyingBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        flyingBears.setCounterCount(CounterType.FLYING, 1);
        Permanent groundBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(flyingBears.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(groundBears.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(gqs.hasKeyword(gd, groundBears, Keyword.FLYING)).isTrue();
        harness.assertNotOnBattlefield(player1, "Song of E\u00e4rendil");
        harness.assertInGraveyard(player1, "Song of E\u00e4rendil");
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SongOfERendil());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
