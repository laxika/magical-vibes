package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorealOutrider.class, GrizzlyBears.class})
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
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Snow mana of a color the creature spell does not have does not grant a counter")
    void nonMatchingColorSnowManaDoesNotGrantCounter() {
        addBorealOutrider();
        ManaPool pool = gd.playerManaPools.get(player1.getId());
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

    @Test
    @DisplayName("No ability triggers when no snow mana was spent")
    void noSnowManaDoesNotTrigger() {
        addBorealOutrider();
        harness.castFromHand(player1, new BorealOutrider(), "{2}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("No ability triggers when snow mana does not match the spell's colors")
    void nonMatchingSnowManaDoesNotTrigger() {
        addBorealOutrider();
        addSnowMana(ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new BorealOutrider()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Each Outrider grants a counter even when only one matching snow mana was spent")
    void multipleOutridersGrantSeparateCounters() {
        addBorealOutrider();
        addBorealOutrider();
        addSnowMana(ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        BorealOutrider creature = new BorealOutrider();
        harness.setHand(player1, List.of(creature));
        harness.castCreature(player1, 0);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
    }

    @Test
    @DisplayName("An Outrider does not trigger for its own cast")
    void doesNotTriggerForItsOwnCast() {
        addSnowMana(ManaColor.GREEN, 3);
        harness.setHand(player1, List.of(new BorealOutrider()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Boreal Outrider")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's Outrider does not trigger for your creature spell")
    void opponentsOutriderDoesNotTrigger() {
        addCreatureReady(player2, new BorealOutrider());
        addSnowMana(ManaColor.GREEN, 3);
        harness.setHand(player1, List.of(new BorealOutrider()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Boreal Outrider")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addBorealOutrider() {
        addCreatureReady(player1, new BorealOutrider());
    }

    private void addSnowMana(ManaColor color, int amount) {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(color, amount);
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
