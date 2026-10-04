package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HedronMatrix.class, GrizzlyBears.class, EliteVanguard.class})
class HedronMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +X/+X equal to its mana value")
    void equippedCreatureGetsItsManaValueAsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new HedronMatrix());
        matrix.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost uses the equipped creature's current mana value")
    void boostUsesEquippedCreatureManaValue() {
        Permanent creature = addCreatureReady(player1, new EliteVanguard());
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new HedronMatrix());
        matrix.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unattached Hedron Matrix does not boost creatures")
    void unattachedMatrixDoesNotBoostCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new HedronMatrix());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void equipResolvesForFourManaAndMovesTheBoost() {
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new HedronMatrix());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, bears.getId());
        assertThat(matrix.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(matrix.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, vanguard.getId());
        harness.passBothPriorities();

        assertThat(matrix.getAttachedTo()).isEqualTo(vanguard.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new HedronMatrix());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(matrix.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedOutsideMainPhase() {
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new HedronMatrix());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(matrix.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void staticBoostStillAppliesToCreatureControlledByOpponent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new HedronMatrix());
        matrix.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }
}
