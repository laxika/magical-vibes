package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamTrawler.class, NyxbornColossus.class})
class DreamTrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card gives Dream Trawler +1/+0 until end of turn")
    void drawingCardBoostsSelf() {
        Permanent trawler = addReadyTrawler(player1);
        gd.playerDecks.get(player1.getId()).add(new NyxbornColossus());
        int basePower = gqs.getEffectivePower(gd, trawler);
        int baseToughness = gqs.getEffectiveToughness(gd, trawler);

        drawAndResolveTrigger(player1);

        assertThat(gqs.getEffectivePower(gd, trawler)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, trawler)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Dream Trawler's attack trigger draws a card")
    void attackingDrawsCard() {
        Permanent trawler = addReadyTrawler(player1);
        gd.playerDecks.get(player1.getId()).add(new NyxbornColossus());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int basePower = gqs.getEffectivePower(gd, trawler);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gqs.getEffectivePower(gd, trawler)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("Discarding a card gives Dream Trawler hexproof and taps it")
    void discardGrantsHexproofAndTapsSelf() {
        Permanent trawler = addReadyTrawler(player1);
        harness.setHand(player1, List.of(new NyxbornColossus()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        assertThat(gqs.hasKeyword(gd, trawler, Keyword.HEXPROOF)).isTrue();
        assertThat(trawler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hexproof from the discard ability wears off at end of turn")
    void hexproofWearsOffAtEndOfTurn() {
        Permanent trawler = addReadyTrawler(player1);
        harness.setHand(player1, List.of(new NyxbornColossus()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, trawler, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, trawler, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the discard ability with an empty hand")
    void cannotActivateWithoutCardInHand() {
        addReadyTrawler(player1);
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each card drawn boosts each Dream Trawler the drawing player controls")
    void multipleDrawsBoostEachTrawler() {
        Permanent first = addReadyTrawler(player1);
        Permanent second = addReadyTrawler(player1);
        Permanent opposing = addReadyTrawler(player2);
        int basePower = gqs.getEffectivePower(gd, first);
        int opposingPower = gqs.getEffectivePower(gd, opposing);
        gd.playerDecks.get(player1.getId()).addAll(List.of(new NyxbornColossus(), new NyxbornColossus()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(opposingPower);
    }

    @Test
    @DisplayName("Drawing during an opponent's turn still boosts Dream Trawler")
    void drawingOnOpponentsTurnBoostsSelf() {
        Permanent trawler = addReadyTrawler(player1);
        int basePower = gqs.getEffectivePower(gd, trawler);
        gd.playerDecks.get(player1.getId()).add(new NyxbornColossus());
        harness.forceActivePlayer(player2);

        drawAndResolveTrigger(player1);

        assertThat(gqs.getEffectivePower(gd, trawler)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("Draw boosts wear off at end of turn")
    void drawBoostWearsOffAtEndOfTurn() {
        Permanent trawler = addReadyTrawler(player1);
        int basePower = gqs.getEffectivePower(gd, trawler);
        gd.playerDecks.get(player1.getId()).add(new NyxbornColossus());
        drawAndResolveTrigger(player1);
        assertThat(gqs.getEffectivePower(gd, trawler)).isEqualTo(basePower + 1);

        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trawler)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Discard is paid immediately but hexproof and tapping wait for resolution")
    void discardIsCostAndTapIsEffect() {
        Permanent trawler = addReadyTrawler(player1);
        harness.setHand(player1, List.of(new NyxbornColossus()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(trawler.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, trawler, Keyword.HEXPROOF)).isFalse();

        resolveAllTriggers();

        assertThat(trawler.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, trawler, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("A tapped summoning-sick Dream Trawler can activate its discard ability")
    void tappedSummoningSickTrawlerCanGainHexproof() {
        Permanent trawler = harness.addToBattlefieldAndReturn(player1, new DreamTrawler());
        trawler.setSummoningSick(true);
        trawler.tap();
        harness.setHand(player1, List.of(new NyxbornColossus()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        assertThat(trawler.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, trawler, Keyword.HEXPROOF)).isTrue();
    }

    private Permanent addReadyTrawler(Player player) {
        return addCreatureReady(player, new DreamTrawler());
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        resolveAllTriggers();
    }
}
