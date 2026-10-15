package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KioraBestsTheSeaGod.class, Forest.class, NyxbornColossus.class})
class KioraBestsTheSeaGodTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates an 8/8 blue Kraken with hexproof")
    void chapterICreatesKraken() {
        Permanent saga = addSagaWithLore(0);

        triggerNextChapter();
        harness.passBothPriorities();

        Permanent kraken = findPermanent(player1, "Kraken");
        assertThat(kraken).isNotNull();
        assertThat(kraken.getCard().getPower()).isEqualTo(8);
        assertThat(kraken.getCard().getToughness()).isEqualTo(8);
        assertThat(kraken.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(kraken.getCard().getSubtypes()).containsExactly(CardSubtype.KRAKEN);
        assertThat(kraken.getCard().getKeywords()).contains(Keyword.HEXPROOF);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    @DisplayName("Chapter II taps only an opponent's nonland permanents and skips their next untap")
    void chapterIITapsNonlandsAndSkipsUntap() {
        Permanent saga = addSagaWithLore(1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentLand.isTapped()).isFalse();

        endTurn();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentLand.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    @DisplayName("Chapter III permanently steals and untaps an opponent's permanent")
    void chapterIIIStealsAndUntapsPermanent() {
        Permanent saga = addSagaWithLore(2);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        opponentCreature.tap();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        harness.assertInGraveyard(player1, "Kiora Bests the Sea God");
    }

    @Test
    @DisplayName("Casting the Saga triggers chapter I as it enters")
    void castingCreatesKrakenOnEntry() {
        harness.castFromHand(player1, new KioraBestsTheSeaGod(), "{5}{U}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Kraken")).isEqualTo(1);
        assertThat(findPermanent(player1, "Kiora Bests the Sea God")
                .getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II locks already tapped permanents but not later arrivals, for only one untap")
    void chapterIILocksOnlyPermanentsPresentAtResolution() {
        Permanent saga = addSagaWithLore(1);
        Permanent alreadyTapped = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        alreadyTapped.tap();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        opponentLand.tap();

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
        Permanent laterArrival = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        laterArrival.tap();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, saga));

        harness.performUntapStep(player2);

        assertThat(alreadyTapped.isTapped()).isTrue();
        assertThat(laterArrival.isTapped()).isFalse();
        assertThat(opponentLand.isTapped()).isFalse();

        harness.performUntapStep(player2);

        assertThat(alreadyTapped.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Chapter II taps hexproof permanents because it targets their controller")
    void chapterIITapsHexproofKraken() {
        Permanent kraken = createOpponentsKraken();
        addSagaWithLore(1);

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(kraken.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(kraken.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Chapter III can steal a land and keeps it after the Saga is sacrificed and the turn ends")
    void chapterIIIPermanentlyStealsLand() {
        Permanent saga = addSagaWithLore(2);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.tap();

        triggerNextChapter();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land).doesNotContain(saga);
        assertThat(land.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Kiora Bests the Sea God");

        endTurn();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("Chapter III cannot target an opponent's hexproof Kraken")
    void chapterIIIExcludesHexproofPermanent() {
        Permanent kraken = createOpponentsKraken();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        addSagaWithLore(2);

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(land.getId()).doesNotContain(kraken.getId());

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kraken);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
    }

    private Permanent createOpponentsKraken() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new KioraBestsTheSeaGod(), "{5}{U}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();
        return findPermanent(player2, "Kraken");
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new KioraBestsTheSeaGod());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void endTurn() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
    }
}
