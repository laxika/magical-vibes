package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelBrute;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
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

@CardUsed({StandTogether.class, DarksteelGargoyle.class, DarksteelBrute.class})
class StandTogetherTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on each of two target creatures")
    void putsCountersOnBothTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new StandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(first.getEffectivePower()).isEqualTo(5);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getEffectivePower()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can target creatures controlled by different players")
    void canTargetCreaturesControlledByDifferentPlayers() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new StandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opponentCreature.getId()));

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Still puts counters on a surviving target when the other target leaves")
    void stillPutsCountersOnSurvivingTargetWhenOtherTargetLeaves() {
        Permanent removedCreature = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        Permanent survivingCreature = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new StandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, List.of(removedCreature.getId(), survivingCreature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(removedCreature);
        harness.passBothPriorities();

        assertThat(removedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(survivingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Requires two target creatures")
    void requiresTwoTargetCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new StandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the same creature twice")
    void cannotTargetSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new StandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelBrute());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new StandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
