package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.MeteorSword;
import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
import com.github.laxika.magicalvibes.cards.y.YuyanArchers;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoggySwampVinebender.class, OtterPenguin.class, YuyanArchers.class, MeteorSword.class})
class FoggySwampVinebenderTest extends BaseCardTest {

    @Test
    @DisplayName("Foggy Swamp Vinebender cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedByPower2OrLess() {
        Permanent vinebender = attackingVinebender();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new OtterPenguin());

        beginBlockerDeclaration();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(vinebender);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Foggy Swamp Vinebender can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPower3OrGreater() {
        Permanent vinebender = attackingVinebender();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new YuyanArchers());

        beginBlockerDeclaration();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(giant);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(vinebender);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(giant.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Waterbend taps five creatures and puts a +1/+1 counter on Foggy Swamp Vinebender")
    void waterbendPutsCounterOnThisCreature() {
        Permanent vinebender = harness.addToBattlefieldAndReturn(player1, new FoggySwampVinebender());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        Permanent fourth = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());

        harness.activateAbility(player1, 0, null, null);

        assertThat(vinebender.isTapped()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(fourth.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(vinebender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, vinebender)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vinebender)).isEqualTo(4);
    }

    @Test
    @DisplayName("The Waterbend ability cannot be activated during an opponent's turn")
    void waterbendIsRestrictedToYourTurn() {
        Permanent vinebender = harness.addToBattlefieldAndReturn(player1, new FoggySwampVinebender());
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only be activated during your turn");
        assertThat(vinebender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Waterbend can be paid entirely with mana during your end step, repeatedly while tapped")
    void waterbendWithManaDuringEndStep() {
        Permanent vinebender = harness.addToBattlefieldAndReturn(player1, new FoggySwampVinebender());
        vinebender.tap();
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.GREEN, 10);

        harness.activateAbility(player1, 0, null, null);
        assertThat(vinebender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(vinebender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(vinebender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(vinebender.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Waterbend combines mana with tapping summoning-sick creatures")
    void waterbendWithManaAndCreatures() {
        Permanent vinebender = harness.addToBattlefieldAndReturn(player1, new FoggySwampVinebender());
        Permanent support = harness.addToBattlefieldAndReturn(player1, new FoggySwampVinebender());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(vinebender.isTapped()).isTrue();
        assertThat(support.isTapped()).isTrue();
        assertThat(vinebender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(vinebender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(support.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Waterbend can tap a noncreature artifact and ignores already tapped creatures")
    void waterbendWithArtifact() {
        Permanent vinebender = harness.addToBattlefieldAndReturn(player1, new FoggySwampVinebender());
        vinebender.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MeteorSword());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(vinebender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Waterbend cannot use an opponent's creatures to cover missing payment")
    void waterbendRequiresEnoughControlledResources() {
        Permanent vinebender = harness.addToBattlefieldAndReturn(player1, new FoggySwampVinebender());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new FoggySwampVinebender());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(vinebender.isTapped()).isFalse();
        assertThat(opponent.isTapped()).isFalse();
        assertThat(vinebender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blocking uses current power, allowing a two-power creature with a +1/+1 counter")
    void blockingUsesCurrentPower() {
        Permanent vinebender = attackingVinebender();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OtterPenguin());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        beginBlockerDeclaration();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(vinebender))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent attackingVinebender() {
        Permanent vinebender = harness.addToBattlefieldAndReturn(player1, new FoggySwampVinebender());
        vinebender.setSummoningSick(false);
        vinebender.setAttacking(true);
        return vinebender;
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
