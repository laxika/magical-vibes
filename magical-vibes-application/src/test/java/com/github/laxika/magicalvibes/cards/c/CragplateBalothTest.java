package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CragplateBaloth.class, Cancel.class, Shock.class})
class CragplateBalothTest extends BaseCardTest {

    @Test
    void castWithoutKickerEntersWithoutCounters() {
        harness.setHand(player1, List.of(new CragplateBaloth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent baloth = findPermanent(player1, "Cragplate Baloth");
        assertThat(baloth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castWithKickerEntersWithFourCounters() {
        harness.setHand(player1, List.of(new CragplateBaloth()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent baloth = findPermanent(player1, "Cragplate Baloth");
        assertThat(baloth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void cannotBeCounteredByCancel() {
        CragplateBaloth baloth = new CragplateBaloth();
        harness.setHand(player1, List.of(baloth));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, baloth.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cragplate Baloth");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void hexproofPreventsOpponentFromTargetingIt() {
        harness.addToBattlefield(player1, new CragplateBaloth());
        Permanent baloth = findPermanent(player1, "Cragplate Baloth");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, baloth.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedSpellCannotBeCounteredAndEntersWithCountersWithoutATrigger() {
        CragplateBaloth baloth = new CragplateBaloth();
        harness.setHand(player1, List.of(baloth));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castKickedCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, baloth.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cragplate Baloth")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void hasteAllowsAttackingOnTheTurnItIsCast() {
        harness.setHand(player1, List.of(new CragplateBaloth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent baloth = findPermanent(player1, "Cragplate Baloth");
        assertThat(baloth.isSummoningSick()).isTrue();
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(baloth.isAttacking()).isTrue();
    }

    @Test
    void hexproofAllowsItsControllerToTargetIt() {
        harness.addToBattlefield(player1, new CragplateBaloth());
        Permanent baloth = findPermanent(player1, "Cragplate Baloth");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, baloth.getId());

        assertThat(baloth.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Cragplate Baloth");
        harness.assertInGraveyard(player1, "Shock");
    }
}
