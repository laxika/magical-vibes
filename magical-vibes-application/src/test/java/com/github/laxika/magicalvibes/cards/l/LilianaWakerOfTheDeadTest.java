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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianaWakerOfTheDead.class, Shock.class, GiantSpider.class, GrizzlyBears.class})
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

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned).isNotNull();
        assertThat(gqs.hasKeyword(gd, returned, com.github.laxika.magicalvibes.model.Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card.getId().equals(creature.getId()));
    }


    @Test
    @DisplayName("+1 keeps choices private until all players have chosen")
    void plusOneDiscardsSimultaneously() {
        addReadyLiliana(player1, 4);
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        boolean firstCardWasRevealed = !gd.playerGraveyards.get(player1.getId()).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(firstCardWasRevealed).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("+1 does not make its empty-handed controller lose life")
    void plusOneDoesNotPenalizeController() {
        addReadyLiliana(player1, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("-3 counts the graveyard at resolution and fixes X afterward")
    void minusThreeFixesGraveyardCountAtResolution() {
        addReadyLiliana(player1, 5);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new Shock(), new Shock(), new Shock()));
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.activateAbility(player1, 0, 1, null, spider.getId());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(4);
    }

    @Test
    @DisplayName("-3 can kill a creature even when spending the last loyalty")
    void minusThreeResolvesAfterLilianaDies() {
        addReadyLiliana(player1, 3);
        harness.setGraveyard(player1, List.of(new Shock()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Liliana, Waker of the Dead");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The emblem triggers only on its controller's turn")
    void emblemDoesNotTriggerOnOpponentsCombat() {
        addReadyLiliana(player1, 7);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The emblem returns your own creature and grants haste indefinitely")
    void emblemReturnsOwnCreatureWithPermanentHaste() {
        addReadyLiliana(player1, 7);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, returned, com.github.laxika.magicalvibes.model.Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The emblem cannot return a target that left the graveyard")
    void emblemDoesNotReturnMissingTarget() {
        addReadyLiliana(player1, 7);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(bears));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }


    @Test
    @DisplayName("-3 with an empty graveyard leaves the target unchanged")
    void minusThreeWithEmptyGraveyard() {
        addReadyLiliana(player1, 5);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new Shock(), new Shock()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent addReadyLiliana(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LilianaWakerOfTheDead());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
