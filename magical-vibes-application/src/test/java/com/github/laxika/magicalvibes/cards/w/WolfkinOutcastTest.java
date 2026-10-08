package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BoundingWolf;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WolfkinOutcast.class, WeddingCrasher.class, BoundingWolf.class, GrizzlyBears.class})
class WolfkinOutcastTest extends BaseCardTest {

    @Test
    void costsTwoLessWithWolfOrWerewolf() {
        harness.addToBattlefield(player1, new BoundingWolf());
        harness.castFromHand(player1, new WolfkinOutcast(), "{3}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotPayReducedCostWithoutWolfOrWerewolf() {
        harness.setHand(player1, List.of(new WolfkinOutcast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void transformsWithDayNight() {
        gd.dayNight = DayNight.DAY;
        Permanent outcast = harness.enterBattlefieldAndReturn(player1, new WolfkinOutcast());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(outcast.getCard()).isInstanceOf(WeddingCrasher.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(outcast.getCard()).isInstanceOf(WolfkinOutcast.class);
    }

    @Test
    void backFaceDrawsWhenAnotherWolfOrWerewolfYouControlDies() {
        Permanent outcast = addWeddingCrasher();
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new BoundingWolf());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        wolf.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(outcast.getCard()).isInstanceOf(WeddingCrasher.class);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void backFaceDoesNotDrawWhenAnotherNonWolfCreatureYouControlDies() {
        addWeddingCrasher();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card drawn = new BoundingWolf();
        harness.setLibrary(player1, List.of(drawn));

        bear.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void backFaceDrawsWhenItDies() {
        Permanent outcast = addWeddingCrasher();
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        outcast.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void costsTwoLessWithAnotherWerewolf() {
        harness.enterBattlefieldAndReturn(player1, new WolfkinOutcast());

        harness.castFromHand(player1, new WolfkinOutcast(), "{3}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsWerewolfDoesNotReduceCost() {
        harness.enterBattlefieldAndReturn(player2, new WolfkinOutcast());

        assertThatThrownBy(() -> harness.castFromHand(player1, new WolfkinOutcast(), "{3}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void multipleWerewolvesDoNotMultiplyReduction() {
        harness.enterBattlefieldAndReturn(player1, new WolfkinOutcast());
        harness.enterBattlefieldAndReturn(player1, new WolfkinOutcast());

        assertThatThrownBy(() -> harness.castFromHand(player1, new WolfkinOutcast(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void fullCostWorksWithoutAnotherWolfOrWerewolf() {
        harness.castFromHand(player1, new WolfkinOutcast(), "{5}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wolfkin Outcast");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
    }

    @Test
    void entersTransformedAtNight() {
        Permanent outcast = addWeddingCrasher();

        assertThat(outcast.isTransformed()).isTrue();
        assertThat(outcast.getCard()).isInstanceOf(WeddingCrasher.class);
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
    }

    @Test
    void backFaceDoesNotDrawForOpponentsWerewolf() {
        addWeddingCrasher();
        Permanent opponent = harness.enterBattlefieldAndReturn(player2, new WolfkinOutcast());
        Card drawn = new WolfkinOutcast();
        harness.setLibrary(player1, List.of(drawn));

        opponent.setMarkedDamage(5);
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
    }

    @Test
    void frontFaceDoesNotDrawWhenAnotherWerewolfDies() {
        harness.enterBattlefieldAndReturn(player1, new WolfkinOutcast());
        Permanent other = harness.enterBattlefieldAndReturn(player1, new WolfkinOutcast());
        Card drawn = new WolfkinOutcast();
        harness.setLibrary(player1, List.of(drawn));

        other.setMarkedDamage(4);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void simultaneousWerewolfDeathsEachTriggerBothCrashers() {
        Permanent first = addWeddingCrasher();
        Permanent second = addWeddingCrasher();
        List<Card> draws = List.of(new WolfkinOutcast(), new WolfkinOutcast(),
                new WolfkinOutcast(), new WolfkinOutcast());
        harness.setLibrary(player1, draws);

        first.setMarkedDamage(5);
        second.setMarkedDamage(5);
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(4);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(draws);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addWeddingCrasher() {
        gd.dayNight = DayNight.NIGHT;
        return harness.enterBattlefieldAndReturn(player1, new WolfkinOutcast());
    }
}
