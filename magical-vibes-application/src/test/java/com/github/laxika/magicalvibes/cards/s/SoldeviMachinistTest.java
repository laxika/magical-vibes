package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoldeviMachinistTest extends BaseCardTest {

    private Permanent machinistOnBattlefield() {
        Permanent machinist = harness.addToBattlefieldAndReturn(player1, new SoldeviMachinist());
        machinist.setSummoningSick(false);
        return machinist;
    }

    private void activateManaAbility() {
        harness.activateAbility(player1, 0, 0, null, null);
    }

    @Test
    @DisplayName("Tap ability adds two artifact-ability-only colorless")
    void tapAddsTwoArtifactAbilityOnlyColorless() {
        machinistOnBattlefield();

        activateManaAbility();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(2);
    }

    @Test
    @DisplayName("Restricted mana pays an artifact's activated ability")
    void paysArtifactActivatedAbility() {
        machinistOnBattlefield();
        Permanent icy = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        icy.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activateManaAbility();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(2);

        // Icy Manipulator is at battlefield index 1; ability 0 costs {1}{T}
        harness.activateAbility(player1, 1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana cannot pay an artifact spell")
    void cannotPayArtifactSpell() {
        machinistOnBattlefield();
        activateManaAbility();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CopperMyr()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(2);
    }

    @Test
    @DisplayName("Restricted mana cannot pay a non-artifact spell")
    void cannotPayNonArtifactSpell() {
        machinistOnBattlefield();
        activateManaAbility();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(2);
    }
}
