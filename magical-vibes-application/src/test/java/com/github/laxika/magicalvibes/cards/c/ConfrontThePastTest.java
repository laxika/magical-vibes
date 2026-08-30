package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KothOfTheHammer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfrontThePastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a planeswalker card with mana value at most X")
    void returnsPlaneswalkerFromGraveyard() {
        KothOfTheHammer koth = new KothOfTheHammer();
        harness.setGraveyard(player1, List.of(koth));
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 4,
                koth.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(koth.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(koth.getId()));
    }

    @Test
    @DisplayName("Rejects a planeswalker card whose mana value is greater than X")
    void rejectsPlaneswalkerAboveX() {
        KothOfTheHammer koth = new KothOfTheHammer();
        harness.setGraveyard(player1, List.of(koth));
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(player1, 0, 1,
                new int[]{0}, 3, koth.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removes twice X loyalty counters from an opponent planeswalker")
    void removesTwiceXLoyalty() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new KothOfTheHammer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 8);
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 2,
                List.of(planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Rejects the loyalty mode targeting your own planeswalker")
    void rejectsOwnPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new KothOfTheHammer());
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(player1, 0, 1,
                new int[]{1}, 2, List.of(planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
