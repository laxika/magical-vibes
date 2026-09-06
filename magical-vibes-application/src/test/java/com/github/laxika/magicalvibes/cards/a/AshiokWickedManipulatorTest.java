package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YawgmothsBargain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshiokWickedManipulator.class, Forest.class, GrizzlyBears.class, Shock.class,
        YawgmothsBargain.class})
class AshiokWickedManipulatorTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts one of the top two cards into hand and exiles the other")
    void plusOneChoosesOneCardForHandAndExilesTheOther() {
        Permanent ashiok = addReadyAshiok(player1, 3);
        Card exiled = new Forest();
        Card toHand = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiled, toHand));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(toHand.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(toHand);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
    }

    @Test
    @DisplayName("-2 creates Nightmare tokens that get counters at combat after an exile")
    void minusTwoCreatesNightmaresThatGetCountersAtBeginningOfCombat() {
        Permanent ashiok = addReadyAshiok(player1, 3);
        gd.addToExile(player1.getId(), new Shock());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> nightmares = findPermanents(player1, "Nightmare");
        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(nightmares).hasSize(2);
        assertThat(nightmares).allSatisfy(token ->
                assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());

        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                        .handleBeginningOfCombatTriggers(gd));
        resolveAllTriggers();

        assertThat(nightmares).allSatisfy(token ->
                assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("-7 exiles as many cards as the mana value of owned exiled cards")
    void minusSevenExilesCardsBasedOnOwnedExile() {
        addReadyAshiok(player1, 7);
        Card ownedExile = new GrizzlyBears();
        gd.addToExile(player1.getId(), ownedExile);
        Card first = new Forest();
        Card second = new Shock();
        Card third = new Forest();
        harness.setLibrary(player2, List.of(first, second, third));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Ashiok replaces a life payment with exiling from the library")
    void replacesLifePaymentWithExile() {
        addReadyAshiok(player1, 3);
        harness.addToBattlefield(player1, new YawgmothsBargain());
        Card paidInstead = new Forest();
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(paidInstead, drawn));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(paidInstead);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyAshiok(Player player, int loyalty) {
        Permanent perm = new Permanent(new AshiokWickedManipulator());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
