package com.github.laxika.magicalvibes.cards.h;

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

@CardUsed({HargildeKindlyRunechanter.class, CopperMyr.class, IcyManipulator.class, GrizzlyBears.class})
class HargildeKindlyRunechanterTest extends BaseCardTest {

    @Test
    void tappingHargildeAddsTwoArtifactSpellOrAbilityRestrictedMana() {
        addReadyHargilde();

        harness.activateAbility(player1, 0, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getArtifactSpellOrAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void restrictedManaCanPayForArtifactSpell() {
        addReadyHargilde();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new CopperMyr()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void restrictedManaCanPayForArtifactAbility() {
        addReadyHargilde();
        addReadyIcyManipulator();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void restrictedManaCannotPayForNonartifactSpell() {
        addReadyHargilde();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);
    }

    private void addReadyHargilde() {
        Permanent hargilde = harness.addToBattlefieldAndReturn(player1, new HargildeKindlyRunechanter());
        hargilde.setSummoningSick(false);
    }

    private void addReadyIcyManipulator() {
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        icyManipulator.setSummoningSick(false);
    }
}
