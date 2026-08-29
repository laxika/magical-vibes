package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectiveEffortTest extends BaseCardTest {

    @Test
    @DisplayName("Destroy creature mode destroys a creature with power 4 or greater")
    void destroysHighPowerCreature() {
        GrizzlyBears bearCard = new GrizzlyBears();
        bearCard.setPower(5);
        bearCard.setToughness(5);
        Permanent bear = addCreatureReady(player2, bearCard);
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0}, List.of(bear.getId()), null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroy enchantment mode destroys an enchantment")
    void destroysEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CageOfHands());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1}, List.of(enchantment.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(enchantment.getId()));
    }

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on each creature target player controls")
    void putsCountersOnTargetPlayersCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2}, List.of(player1.getId()), null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple modes tap one creature for each extra mode")
    void multipleModesPayWithCreatureTaps() {
        Permanent firstTapper = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTapper = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears bearCard = new GrizzlyBears();
        bearCard.setPower(5);
        bearCard.setToughness(5);
        Permanent targetCreature = addCreatureReady(player2, bearCard);
        Permanent targetEnchantment = harness.addToBattlefieldAndReturn(player2, new CageOfHands());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        harness.castModalSorceryWithModesAndTaps(player1, 0, 1, 3, new int[]{0, 1, 2},
                List.of(targetCreature.getId(), targetEnchantment.getId(), player1.getId()),
                List.of(firstTapper.getId(), secondTapper.getId()));
        harness.passBothPriorities();

        assertThat(firstTapper.isTapped()).isTrue();
        assertThat(secondTapper.isTapped()).isTrue();
        assertThat(firstTapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondTapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetEnchantment.getId()));
    }

    @Test
    @DisplayName("A creature below power 4 cannot be targeted by the destroy creature mode")
    void rejectsLowPowerCreatureTarget() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{0}, List.of(bear.getId()), null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("Choosing multiple modes without enough creatures to tap is rejected")
    void rejectsMultipleModesWithoutEscalateTaps() {
        GrizzlyBears bearCard = new GrizzlyBears();
        bearCard.setPower(5);
        bearCard.setToughness(5);
        Permanent targetCreature = addCreatureReady(player2, bearCard);
        Permanent targetEnchantment = harness.addToBattlefieldAndReturn(player2, new CageOfHands());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModesAndTaps(player1, 0, 1, 3,
                new int[]{0, 1}, List.of(targetCreature.getId(), targetEnchantment.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
