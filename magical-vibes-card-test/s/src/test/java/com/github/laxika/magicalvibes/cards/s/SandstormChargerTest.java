package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BreakOpen;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandstormCharger.class, BreakOpen.class})
class SandstormChargerTest extends BaseCardTest {

    @Test
    void megamorphsFaceDownAndPutsACounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new SandstormCharger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Sandstorm Charger");
        assertThat(charger.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int chargerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(charger);
        harness.turnFaceUp(player1, chargerIndex);
        harness.passBothPriorities();

        assertThat(charger.isFaceDown()).isFalse();
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void megamorphCounterIsPlacedImmediatelyWithoutUsingTheStack() {
        harness.setHand(player1, List.of(new SandstormCharger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Sandstorm Charger");
        assertThat(charger.isFaceDown()).isTrue();
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(charger));

        assertThat(charger.isFaceDown()).isFalse();
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turningFaceUpWithBreakOpenDoesNotGrantAMegamorphCounter() {
        harness.setHand(player1, List.of(new SandstormCharger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Sandstorm Charger");
        assertThat(charger.isFaceDown()).isTrue();
        harness.setHand(player2, List.of(new BreakOpen()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, charger.getId());

        assertThat(charger.isFaceDown()).isFalse();
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Break Open");
    }

    @Test
    void castingFaceUpDoesNotGrantAMegamorphCounter() {
        harness.setHand(player1, List.of(new SandstormCharger()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Sandstorm Charger");
        assertThat(charger.isFaceDown()).isFalse();
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
