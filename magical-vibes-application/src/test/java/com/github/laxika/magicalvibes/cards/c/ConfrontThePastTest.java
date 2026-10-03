package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KasminaEnigmaSage;
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

@CardUsed({ConfrontThePast.class, KasminaEnigmaSage.class})
class ConfrontThePastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a planeswalker card with mana value at most X")
    void returnsPlaneswalkerFromGraveyard() {
        KasminaEnigmaSage kasmina = new KasminaEnigmaSage();
        harness.setGraveyard(player1, List.of(kasmina));
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 4,
                kasmina.getId(), List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kasmina, Enigma Sage");
        harness.assertNotInGraveyard(player1, "Kasmina, Enigma Sage");
    }

    @Test
    @DisplayName("Rejects a planeswalker card whose mana value is greater than X")
    void rejectsPlaneswalkerAboveX() {
        KasminaEnigmaSage kasmina = new KasminaEnigmaSage();
        harness.setGraveyard(player1, List.of(kasmina));
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(player1, 0, 1,
                new int[]{0}, 2, kasmina.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removes twice X loyalty counters from an opponent planeswalker")
    void removesTwiceXLoyalty() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new KasminaEnigmaSage());
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
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new KasminaEnigmaSage());
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(player1, 0, 1,
                new int[]{1}, 2, List.of(planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsPlaneswalkerWithManaValueExactlyX() {
        KasminaEnigmaSage kasmina = new KasminaEnigmaSage();
        harness.setGraveyard(player1, List.of(kasmina));
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 3,
                kasmina.getId(), List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kasmina, Enigma Sage");
        harness.assertNotInGraveyard(player1, "Kasmina, Enigma Sage");
    }

    @Test
    void rejectsPlaneswalkerInOpponentsGraveyard() {
        KasminaEnigmaSage kasmina = new KasminaEnigmaSage();
        harness.setGraveyard(player2, List.of(kasmina));
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(player1, 0, 1,
                new int[]{0}, 3, kasmina.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNonPlaneswalkerInGraveyard() {
        ConfrontThePast sorcery = new ConfrontThePast();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(player1, 0, 1,
                new int[]{0}, 3, sorcery.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroXLeavesLoyaltyUnchanged() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new KasminaEnigmaSage());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 0,
                List.of(planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Kasmina, Enigma Sage");
    }

    @Test
    void removesAvailableLoyaltyAndPlaneswalkerDies() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new KasminaEnigmaSage());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 2,
                List.of(planeswalker.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kasmina, Enigma Sage");
        harness.assertInGraveyard(player2, "Kasmina, Enigma Sage");
    }

    @Test
    void removesOnlyLoyaltyCounters() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new KasminaEnigmaSage());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        planeswalker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 1,
                List.of(planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(planeswalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotReturnAnotherCardWhenGraveyardTargetDisappears() {
        KasminaEnigmaSage target = new KasminaEnigmaSage();
        KasminaEnigmaSage other = new KasminaEnigmaSage();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new ConfrontThePast()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 3,
                target.getId(), List.of());
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kasmina, Enigma Sage");
        harness.assertInGraveyard(player1, "Kasmina, Enigma Sage");
    }
}
