package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PouncingKavu.class, NomadicElf.class})
class PouncingKavuTest extends BaseCardTest {

    @Test
    void castWithoutKickerEntersWithoutCountersOrHaste() {
        harness.setHand(player1, List.of(new PouncingKavu()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent pouncingKavu = findPouncingKavu();
        assertThat(pouncingKavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, pouncingKavu, Keyword.HASTE)).isFalse();
    }

    @Test
    void castWithKickerEntersWithTwoCountersAndHaste() {
        harness.setHand(player1, List.of(new PouncingKavu()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent pouncingKavu = findPouncingKavu();
        assertThat(pouncingKavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, pouncingKavu, Keyword.HASTE)).isTrue();
    }

    @Test
    void castWithKickerRequiresItsFullAdditionalCost() {
        harness.setHand(player1, List.of(new PouncingKavu()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedCreatureCanAttackTheTurnItEnters() {
        harness.setHand(player1, List.of(new PouncingKavu()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    void unkickedCreatureCannotAttackTheTurnItEnters() {
        harness.setHand(player1, List.of(new PouncingKavu()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    @Test
    void kickedCreatureKillsBlockerBeforeItCanDealCombatDamage() {
        addCreatureReady(player2, new NomadicElf());
        harness.setHand(player1, List.of(new PouncingKavu()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Nomadic Elf");
        harness.assertOnBattlefield(player1, "Pouncing Kavu");
        assertThat(findPouncingKavu().getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    void enteringWithoutBeingCastDoesNotGrantKickerBonuses() {
        Permanent pouncingKavu = harness.enterBattlefieldAndReturn(player1, new PouncingKavu());

        assertThat(pouncingKavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, pouncingKavu, Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent findPouncingKavu() {
        return findPermanent(player1, "Pouncing Kavu");
    }
}
