package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UntamedKavu.class})
class UntamedKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Kicker consumes three additional generic mana")
    void kickerConsumesThreeAdditionalMana() {
        harness.setHand(player1, List.of(new UntamedKavu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Untamed Kavu")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cast without kicker enters with no counters")
    void castWithoutKicker() {
        harness.setHand(player1, List.of(new UntamedKavu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Untamed Kavu");
        assertThat(kavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cast with kicker enters with three counters without a counter placement trigger")
    void castWithKicker() {
        harness.setHand(player1, List.of(new UntamedKavu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Untamed Kavu");
        assertThat(kavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cast with kicker but not enough mana throws exception")
    void castWithKickerNotEnoughMana() {
        harness.setHand(player1, List.of(new UntamedKavu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Entering without being cast does not grant kicker counters")
    void enteringWithoutCastingHasNoCounters() {
        Permanent kavu = harness.enterBattlefieldAndReturn(player1, new UntamedKavu());

        assertThat(kavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A kicked Kavu attacks without tapping and tramples over an unkicked Kavu")
    void kickedKavuHasVigilanceAndTrampleInCombat() {
        harness.setHand(player1, List.of(new UntamedKavu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent attacker = findPermanent(player1, "Untamed Kavu");
        attacker.setSummoningSick(false);
        addCreatureReady(player2, new UntamedKavu());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(attacker.isTapped()).isFalse();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player1, "Untamed Kavu");
        harness.assertInGraveyard(player2, "Untamed Kavu");
    }
}
