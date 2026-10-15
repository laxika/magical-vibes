package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RailwayBrawler.class, GrizzlyBears.class, GiantGrowth.class})
class RailwayBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters on another entering creature equal to its power")
    void putsCountersEqualToEnteringPower() {
        harness.addToBattlefield(player1, new RailwayBrawler());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Uses the entering creature's power when the trigger resolves")
    void usesPowerAtResolution() {
        harness.addToBattlefield(player1, new RailwayBrawler());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger for its own entry or an opponent's creature")
    void doesNotTriggerForOwnOrOpponentEntry() {
        harness.castFromHand(player1, new RailwayBrawler(), "{3}{G}{G}");
        harness.passBothPriorities();

        Permanent brawler = findPermanent(player1, "Railway Brawler");
        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");
        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(RailwayBrawler.class)
    void multipleBrawlersEachUsePowerAtTheirOwnResolution() {
        harness.addToBattlefield(player1, new RailwayBrawler());
        harness.addToBattlefield(player1, new RailwayBrawler());
        RailwayBrawler entering = new RailwayBrawler();
        harness.castFromHand(player1, entering, "{3}{G}{G}");
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == entering).findFirst().orElseThrow();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.passBothPriorities();
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(15);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(RailwayBrawler.class)
    void plotPaysFourManaAndAllowsFreeCastOnlyOnALaterTurn() {
        RailwayBrawler brawler = new RailwayBrawler();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(brawler));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Railway Brawler");
        assertThatThrownBy(() -> harness.castFromExile(player1, brawler.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, brawler.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Railway Brawler");
        assertThat(findPermanent(player1, "Railway Brawler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.castFromHand(player1, new RailwayBrawler(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(p -> assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5));
    }
}
