package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferiTemporalArchmage.class, TeferiHeroOfDominaria.class, GrizzlyBears.class, Forest.class})
class TeferiTemporalArchmageTest extends BaseCardTest {

    @Test
    void plusOnePutsOneOfTheTopTwoCardsIntoHandAndTheOtherOnTheBottom() {
        Permanent teferi = addReadyTeferi(player1, 5);
        Card first = new GrizzlyBears();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId())).contains(first);
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void minusOneUntapsUpToFourTargetPermanents() {
        Permanent teferi = addReadyTeferi(player1, 5);
        Permanent bear1 = addTapped(player1, new GrizzlyBears());
        Permanent bear2 = addTapped(player1, new GrizzlyBears());
        Permanent forest1 = addTapped(player1, new Forest());
        Permanent forest2 = addTapped(player1, new Forest());

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(bear1.getId(), bear2.getId(), forest1.getId(), forest2.getId()));
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(bear1.isTapped()).isFalse();
        assertThat(bear2.isTapped()).isFalse();
        assertThat(forest1.isTapped()).isFalse();
        assertThat(forest2.isTapped()).isFalse();
    }

    @Test
    void minusOneMayChooseNoTargets() {
        Permanent teferi = addReadyTeferi(player1, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void minusOneRejectsANonPermanentTarget() {
        addReadyTeferi(player1, 5);
        Card card = new GrizzlyBears();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(card.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ultimateEmblemAllowsLoyaltyAbilitiesOnAnOpponentsTurn() {
        addReadyTeferi(player1, 10);
        Permanent hero = addReadyHero(player1, 4);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(hero.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    private Permanent addReadyTeferi(Player player, int loyalty) {
        Permanent permanent = new Permanent(new TeferiTemporalArchmage());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }

    private Permanent addReadyHero(Player player, int loyalty) {
        Permanent permanent = new Permanent(new TeferiHeroOfDominaria());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.tap();
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
