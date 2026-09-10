package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RalZarekGuestLecturerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 surveils two cards")
    void plusOneSurveilsTwo() {
        addReadyRal(player1, 3);
        Card topCard = new GrizzlyBears();
        Card secondCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("-1 makes each target player discard a card")
    void minusOneMakesEachTargetPlayerDiscard() {
        Permanent ral = addReadyRal(player1, 1);
        harness.setHand(player1, List.of(new Swamp()));
        harness.setHand(player2, List.of(new Swamp()));

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-2 returns a creature card with mana value three or less")
    void minusTwoReturnsCheapCreature() {
        Permanent ral = addReadyRal(player1, 2);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.activateAbility(player1, 0, 2, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent ->
                assertThat(permanent.getCard().getId()).isEqualTo(bears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-7 skips the target opponent's next turn for each head")
    void minusSevenSkipsTurnsForHeads() {
        Permanent ral = addReadyRal(player1, 7);

        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.skipNextTurnCount.getOrDefault(player2.getId(), 0)).isBetween(0, 5);
        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private Permanent addReadyRal(Player player, int loyalty) {
        Permanent perm = new Permanent(new RalZarekGuestLecturer());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
