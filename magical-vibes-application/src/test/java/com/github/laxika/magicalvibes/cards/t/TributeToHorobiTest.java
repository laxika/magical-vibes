package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EchoOfDeathsWail;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TributeToHorobi.class, EchoOfDeathsWail.class, GrizzlyBears.class, TyphoidRats.class})
class TributeToHorobiTest extends BaseCardTest {

    @Test
    void chaptersOneAndTwoCreateRatRogueTokensForEachOpponent() {
        addSagaWithLore(0);
        advanceToNextChapter(player1);
        harness.passBothPriorities();

        assertRatTokens(player2, 1);

        Permanent saga = findSaga(player1);
        saga.setCounterCount(CounterType.LORE, 1);
        advanceToNextChapter(player1);
        harness.passBothPriorities();

        assertRatTokens(player2, 2);
    }

    @Test
    void chapterThreeTransformsIntoEchoOfDeathsWail() {
        addSagaWithLore(2);
        advanceToNextChapter(player1);
        harness.passBothPriorities();

        Permanent echo = findPermanent(player1, "Echo of Death's Wail");
        assertThat(echo).isNotNull();
        assertThat(echo.isTransformed()).isTrue();
    }

    @Test
    void echoGainsControlOfAllRatTokensWhenItEnters() {
        addSagaWithLore(0);
        advanceToNextChapter(player1);
        harness.passBothPriorities();

        Permanent ratToken = findRatTokens(player2).getFirst();
        Permanent nontokenRat = harness.addToBattlefieldAndReturn(player2, new TyphoidRats());
        harness.enterBattlefieldAndReturn(player1, new EchoOfDeathsWail());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ratToken);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nontokenRat);
    }

    @Test
    void echoMaySacrificeAnotherCreatureToDraw() {
        Permanent echo = harness.addToBattlefieldAndReturn(player1, new EchoOfDeathsWail());
        echo.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .filteredOn(name -> name.equals("Grizzly Bears"))
                .hasSize(1);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TributeToHorobi());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent findSaga(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof TributeToHorobi)
                .findFirst()
                .orElseThrow();
    }

    private List<Permanent> findRatTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.RAT))
                .toList();
    }

    private void assertRatTokens(Player player, int count) {
        assertThat(findRatTokens(player)).hasSize(count);
        assertThat(findRatTokens(player)).allMatch(permanent ->
                permanent.getCard().getSubtypes().contains(CardSubtype.ROGUE));
    }

    private void advanceToNextChapter(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
