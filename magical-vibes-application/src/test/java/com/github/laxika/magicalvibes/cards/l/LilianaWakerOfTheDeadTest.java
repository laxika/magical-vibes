package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LilianaWakerOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("+1 makes an opponent with no cards lose 3 life")
    void plusOneMakesEmptyHandedOpponentLoseLife() {
        Permanent liliana = addReadyLiliana(player1, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 makes each player discard when both have cards")
    void plusOneMakesBothPlayersDiscard() {
        addReadyLiliana(player1, 3);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("-3 gives a creature -X/-X based on cards in the controller's graveyard")
    void minusThreeUsesGraveyardSize() {
        Permanent liliana = addReadyLiliana(player1, 5);
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.activateAbility(player1, 0, 1, null, spider.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(2);
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-7 emblem returns a target creature from any graveyard at beginning of combat")
    void minusSevenEmblemReturnsCreatureWithHaste() {
        addReadyLiliana(player1, 7);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(new Shock(), creature));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned).isNotNull();
        assertThat(gqs.hasKeyword(gd, returned, com.github.laxika.magicalvibes.model.Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card.getId().equals(creature.getId()));
    }

    private Permanent addReadyLiliana(Player player, int loyalty) {
        Permanent perm = new Permanent(new LilianaWakerOfTheDead());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
