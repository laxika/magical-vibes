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
        harness.setHand(player1, List.of(new WolfkinOutcast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

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

    private Permanent addWeddingCrasher() {
        gd.dayNight = DayNight.NIGHT;
        return harness.enterBattlefieldAndReturn(player1, new WolfkinOutcast());
    }
}
