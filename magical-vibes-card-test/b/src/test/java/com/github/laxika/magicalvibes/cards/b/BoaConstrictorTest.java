package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BoaConstrictor.class)
class BoaConstrictorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +3/+3 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent constrictor = addCreatureReady(player1, new BoaConstrictor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, constrictor)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, constrictor)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability taps Boa Constrictor")
    void abilityTapsConstrictor() {
        Permanent constrictor = addCreatureReady(player1, new BoaConstrictor());

        harness.activateAbility(player1, 0, null, null);

        assertThat(constrictor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent constrictor = addCreatureReady(player1, new BoaConstrictor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, constrictor)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, constrictor)).isZero();
    }

    @Test
    @DisplayName("Cannot activate while Boa Constrictor has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BoaConstrictor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate while Boa Constrictor is already tapped")
    void cannotActivateWhileTapped() {
        Permanent constrictor = addCreatureReady(player1, new BoaConstrictor());
        constrictor.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("The boost waits for resolution and affects only its source")
    void boostWaitsForResolutionAndOnlyAffectsSource() {
        Permanent constrictor = addCreatureReady(player1, new BoaConstrictor());
        Permanent other = addCreatureReady(player1, new BoaConstrictor());
        Permanent opposing = addCreatureReady(player2, new BoaConstrictor());

        harness.activateAbility(player1, 0, null, null);

        assertThat(PermanentGrants.powerBoostOf(gd, constrictor)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, constrictor)).isZero();

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, constrictor)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, constrictor)).isEqualTo(3);
        assertThat(PermanentGrants.powerBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opposing)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opposing)).isZero();
    }

    @Test
    @DisplayName("Untapping allows another activation and the boosts accumulate")
    void boostsAccumulateAfterUntapping() {
        Permanent constrictor = addCreatureReady(player1, new BoaConstrictor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        constrictor.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, constrictor)).isEqualTo(6);
        assertThat(PermanentGrants.toughnessBoostOf(gd, constrictor)).isEqualTo(6);
        assertThat(constrictor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An ability whose source has left does not boost another Boa Constrictor")
    void departedSourceDoesNotBoostAnotherConstrictor() {
        Permanent constrictor = addCreatureReady(player1, new BoaConstrictor());
        Permanent other = addCreatureReady(player1, new BoaConstrictor());

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(constrictor);
        gd.playerGraveyards.get(player1.getId()).add(constrictor.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isZero();
    }
}
