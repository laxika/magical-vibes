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

        Permanent baloth = findBaloth(player1);
        assertThat(baloth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castWithKickerEntersWithFourCounters() {
        harness.setHand(player1, List.of(new CragplateBaloth()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent baloth = findBaloth(player1);
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

        assertThat(findBaloth(player1)).isNotNull();
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void hexproofPreventsOpponentFromTargetingIt() {
        harness.addToBattlefield(player1, new CragplateBaloth());
        Permanent baloth = findBaloth(player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, baloth.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent findBaloth(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Cragplate Baloth"))
                .findFirst()
                .orElse(null);
    }
}
