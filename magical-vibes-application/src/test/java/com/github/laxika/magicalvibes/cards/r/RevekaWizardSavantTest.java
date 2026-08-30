package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AnabaShaman;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RevekaWizardSavant.class, AnabaShaman.class})
class RevekaWizardSavantTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 2 damage to a target player")
    void abilityDamagesPlayer() {
        Permanent reveka = setUpReveka();

        harness.activateAbility(player1, indexOf(reveka), 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ability deals 2 damage to a target creature, killing a 2/2")
    void abilityKillsCreature() {
        Permanent reveka = setUpReveka();
        Permanent target = addCreatureReady(player2, new AnabaShaman());
        harness.activateAbility(player1, indexOf(reveka), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Reveka taps and does not untap during its controller's next untap step")
    void abilityLocksUntap() {
        Permanent reveka = setUpReveka();

        harness.activateAbility(player1, indexOf(reveka), 0, null, player2.getId());
        assertThat(reveka.isTapped()).isTrue();

        harness.passBothPriorities();
        assertThat(reveka.getSkipUntapCount()).isEqualTo(1);

        advanceToUpkeep(player1);

        assertThat(reveka.isTapped()).isTrue();
        assertThat(reveka.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Ability cannot be activated with summoning sickness")
    void abilityRequiresNoSummoningSickness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new RevekaWizardSavant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent setUpReveka() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return addCreatureReady(player1, new RevekaWizardSavant());
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
