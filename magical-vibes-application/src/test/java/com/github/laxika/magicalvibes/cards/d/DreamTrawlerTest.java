package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DreamTrawler.class, GrizzlyBears.class})
class DreamTrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card gives Dream Trawler +1/+0 until end of turn")
    void drawingCardBoostsSelf() {
        Permanent trawler = addReadyTrawler(player1);
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
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
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int basePower = gqs.getEffectivePower(gd, trawler);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gqs.getEffectivePower(gd, trawler)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("Discarding a card gives Dream Trawler hexproof and taps it")
    void discardGrantsHexproofAndTapsSelf() {
        Permanent trawler = addReadyTrawler(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, trawler, Keyword.HEXPROOF)).isTrue();
        assertThat(trawler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hexproof from the discard ability wears off at end of turn")
    void hexproofWearsOffAtEndOfTurn() {
        Permanent trawler = addReadyTrawler(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

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

    private Permanent addReadyTrawler(Player player) {
        return addCreatureReady(player, new DreamTrawler());
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
