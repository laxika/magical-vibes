package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({FoggySwampVinebender.class, GrizzlyBears.class, HillGiant.class})
class FoggySwampVinebenderTest extends BaseCardTest {

    @Test
    @DisplayName("Foggy Swamp Vinebender cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedByPower2OrLess() {
        Permanent vinebender = attackingVinebender();
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

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
        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);

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
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fourth = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

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
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vinebender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent attackingVinebender() {
        Permanent vinebender = new Permanent(new FoggySwampVinebender());
        vinebender.setSummoningSick(false);
        vinebender.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(vinebender);
        return vinebender;
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
