package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RampagingFerocidon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AjaniUnyielding.class, ChandraNalaar.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class AjaniUnyieldingTest extends BaseCardTest {

    @Test
    @DisplayName("+2 puts revealed nonland permanents into hand and the rest on the library bottom")
    void plusTwoRevealsNonlandPermanents() {
        Permanent ajani = addReadyAjani(player1, 4);
        Card bear = new GrizzlyBears();
        Card chandra = new ChandraNalaar();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(bear, chandra, forest));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(bear, chandra);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("-2 exiles a creature and its controller gains life equal to its power")
    void minusTwoExilesCreatureAndGainsLife() {
        Permanent ajani = addReadyAjani(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == target.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("-9 puts five counters on own creatures and another planeswalker only")
    void minusNinePutsCountersOnOwnCreaturesAndOtherPlaneswalkers() {
        Permanent ajani = addReadyAjani(player1, 10);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
    }

    @Test
    @CardUsed({Shock.class})
    void plusTwoExcludesInstantsAndAllowsOrderingTheBottomCards() {
        addReadyAjani(player1, 4);
        Card bear = new GrizzlyBears();
        Card shock = new Shock();
        Card forest = new Forest();
        Card untouched = new HillGiant();
        harness.setLibrary(player1, List.of(bear, shock, forest, untouched));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, forest, shock);
    }

    @Test
    void plusTwoWithShortLibraryPutsEveryEligibleCardIntoHand() {
        addReadyAjani(player1, 4);
        Card bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void minusTwoUsesModifiedPowerAndCanTargetOwnCreature() {
        addReadyAjani(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == target.getCard());
    }

    @Test
    @CardUsed({RampagingFerocidon.class})
    void minusTwoExilesLifeGainRestrictionBeforeGainingLife() {
        addReadyAjani(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingFerocidon());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == target.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }

    @Test
    void minusNineResolvesAfterAjaniDiesFromPayingItsLastLoyalty() {
        Permanent ajani = addReadyAjani(player1, 9);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        Permanent opposingChandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 3);
        opposingChandra.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ajani);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
        assertThat(opposingChandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    private Permanent addReadyAjani(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AjaniUnyielding());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
