package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelswarmOperator.class, CopperMyr.class, IcyManipulator.class, GrizzlyBears.class})
class SteelswarmOperatorTest extends BaseCardTest {

    @Test
    void firstAbilityAddsArtifactSpellOnlyMana() {
        addReadyOperator();

        harness.activateAbility(player1, 0, 0, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getArtifactSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getArtifactAbilityOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    void firstAbilityManaCastsArtifactButCannotPayArtifactAbility() {
        addReadyOperator();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CopperMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    void firstAbilityManaCannotPayArtifactAbility() {
        addReadyOperator();
        addReadyIcyManipulator();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void secondAbilityManaPaysArtifactAbility() {
        addReadyOperator();
        addReadyIcyManipulator();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void secondAbilityManaCannotCastArtifactSpell() {
        addReadyOperator();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CopperMyr()));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(2);
    }

    private void addReadyOperator() {
        Permanent operator = harness.addToBattlefieldAndReturn(player1, new SteelswarmOperator());
        operator.setSummoningSick(false);
    }

    private void addReadyIcyManipulator() {
        Permanent icy = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        icy.setSummoningSick(false);
    }
}
