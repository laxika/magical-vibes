package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BorealOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("A creature spell cast with matching-color snow mana enters with an additional counter")
    void matchingColorSnowManaGrantsCounter() {
        addBorealOutrider();
        addSnowMana(ManaColor.GREEN, 1);
        gd.playerManaPools.get(player1.getId()).add(ManaColor.GREEN);

        castGrizzlyBears();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature spell cast without snow mana does not enter with an additional counter")
    void noSnowManaDoesNotGrantCounter() {
        addBorealOutrider();
        harness.addMana(player1, ManaColor.GREEN, 2);

        castGrizzlyBears();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Snow mana of a color the creature spell does not have does not grant a counter")
    void nonMatchingColorSnowManaDoesNotGrantCounter() {
        addBorealOutrider();
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.BLUE);
        pool.addSnowMana(ManaColor.BLUE, 1);
        pool.add(ManaColor.GREEN);

        castGrizzlyBears();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple matching snow mana still grants only one additional counter")
    void multipleMatchingSnowManaGrantsOneCounter() {
        addBorealOutrider();
        addSnowMana(ManaColor.GREEN, 2);

        castGrizzlyBears();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void addBorealOutrider() {
        addCreatureReady(player1, new BorealOutrider());
    }

    private void addSnowMana(ManaColor color, int amount) {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(color, amount);
        pool.addSnowMana(color, amount);
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
