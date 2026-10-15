package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NeedleSpires;
import com.github.laxika.magicalvibes.cards.k.KorScythemaster;
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

@CardUsed({ShoulderToShoulder.class, KorScythemaster.class, NeedleSpires.class})
class ShoulderToShoulderTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each of two target creatures and draws a card")
    void supportsTwoCreaturesAndDrawsCard() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KorScythemaster());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new KorScythemaster());
        castShoulderToShoulder(List.of(first.getId(), second.getId()), new KorScythemaster());

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Kor Scythemaster");
    }

    @Test
    @DisplayName("May choose no creatures and still draws a card")
    void mayChooseNoCreatures() {
        castShoulderToShoulder(List.of(), new KorScythemaster());

        harness.assertInHand(player1, "Kor Scythemaster");
        harness.assertInGraveyard(player1, "Shoulder to Shoulder");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new NeedleSpires());
        harness.setHand(player1, List.of(new ShoulderToShoulder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void supportsOneCreatureAndDrawsExactlyOneCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KorScythemaster());
        castShoulderToShoulder(List.of(creature.getId()), new KorScythemaster());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Kor Scythemaster");
    }

    @Test
    void remainingLegalTargetGetsCounterAndControllerDraws() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KorScythemaster());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new KorScythemaster());
        prepareSpell();
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.setGraveyard(player1, List.of(first.getCard()));
        harness.passBothPriorities();

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Kor Scythemaster");
    }

    @Test
    void doesNotDrawWhenAllChosenTargetsLeave() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KorScythemaster());
        prepareSpell();
        harness.castSorcery(player1, 0, List.of(creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.setGraveyard(player1, List.of(creature.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Shoulder to Shoulder");
    }

    @Test
    void cannotChooseMoreThanTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KorScythemaster());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KorScythemaster());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new KorScythemaster());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseTheSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KorScythemaster());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new ShoulderToShoulder()));
        harness.setLibrary(player1, List.of(new KorScythemaster()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void castShoulderToShoulder(List<java.util.UUID> targetIds, KorScythemaster cardToDraw) {
        harness.setHand(player1, List.of(new ShoulderToShoulder()));
        harness.setLibrary(player1, List.of(cardToDraw));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }
}
