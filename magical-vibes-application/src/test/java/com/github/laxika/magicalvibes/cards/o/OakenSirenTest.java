package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OakenSiren.class, CopperMyr.class, IcyManipulator.class, GrizzlyBears.class})
class OakenSirenTest extends BaseCardTest {

    private void addReadySiren() {
        Permanent siren = harness.addToBattlefieldAndReturn(player1, new OakenSiren());
        siren.setSummoningSick(false);
    }

    private void activateForBlue() {
        harness.activateAbility(player1, 0, null, null);
    }

    @Test
    @DisplayName("Tapping Oaken Siren adds blue artifact-restricted mana")
    void addsRestrictedBlueMana() {
        addReadySiren();

        activateForBlue();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana pays for an artifact spell")
    void paysArtifactSpell() {
        addReadySiren();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CopperMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateForBlue();
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Restricted mana pays for an artifact's activated ability")
    void paysArtifactActivatedAbility() {
        addReadySiren();
        Permanent icy = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        icy.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activateForBlue();
        harness.activateAbility(player1, 1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot pay for a nonartifact spell")
    void cannotPayNonartifactSpell() {
        addReadySiren();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        activateForBlue();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }
}
