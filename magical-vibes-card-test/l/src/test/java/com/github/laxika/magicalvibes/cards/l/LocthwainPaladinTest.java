package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LocthwainPaladin.class})
class LocthwainPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter when at least three black mana is spent")
    void entersWithCounterWhenThreeBlackManaIsSpent() {
        harness.setHand(player1, List.of(new LocthwainPaladin()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Locthwain Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not enter with a counter when fewer than three black mana is spent")
    void doesNotEnterWithCounterWhenFewerThanThreeBlackManaIsSpent() {
        harness.setHand(player1, List.of(new LocthwainPaladin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Locthwain Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void twoBlackManaIsNotEnoughForAdamant() {
        harness.setHand(player1, List.of(new LocthwainPaladin()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Locthwain Paladin")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void fourBlackManaStillGivesExactlyOneCounter() {
        harness.setHand(player1, List.of(new LocthwainPaladin()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Locthwain Paladin")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastDoesNotGiveAnAdamantCounter() {
        Permanent paladin = harness.enterBattlefieldAndReturn(player1, new LocthwainPaladin());

        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new LocthwainPaladin());
        addCreatureReady(player2, new LocthwainPaladin());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new LocthwainPaladin());
        Permanent first = addCreatureReady(player2, new LocthwainPaladin());
        Permanent second = addCreatureReady(player2, new LocthwainPaladin());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
