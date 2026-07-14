package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlightedShamanTest extends BaseCardTest {

    // ===== {T}, Sacrifice a Swamp: Target creature gets +1/+1 =====

    @Test
    @DisplayName("Sacrificing a Swamp gives target creature +1/+1 and taps Blighted Shaman")
    void sacrificeSwampGivesPlusOne() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new BlightedShaman());
        shaman.setSummoningSick(false);
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // permanentIndex 0 = Shaman, abilityIndex 0 = sacrifice a Swamp; only 1 Swamp → auto-sacrifice
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.hasType(CardType.LAND));
    }

    // ===== {T}, Sacrifice a creature: Target creature gets +2/+2 =====

    @Test
    @DisplayName("Sacrificing a creature gives target creature +2/+2")
    void sacrificeCreatureGivesPlusTwo() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new BlightedShaman());
        shaman.setSummoningSick(false);
        // Only creature player1 controls is the Shaman → the sacrifice cost auto-picks it.
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // abilityIndex 1 = sacrifice a creature
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        // The Shaman was the sacrificed creature.
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isNotEmpty();
    }

    // ===== Boost wears off at end of turn =====

    @Test
    @DisplayName("The +1/+1 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new BlightedShaman());
        shaman.setSummoningSick(false);
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Cost cannot be paid without a Swamp =====

    @Test
    @DisplayName("The Swamp ability cannot be activated without a Swamp to sacrifice")
    void cannotActivateSwampAbilityWithoutSwamp() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new BlightedShaman());
        shaman.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId())
        ).isInstanceOf(IllegalStateException.class);
    }
}
