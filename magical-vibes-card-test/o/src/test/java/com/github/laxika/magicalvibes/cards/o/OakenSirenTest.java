package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CaptivatingCave;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
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

@CardUsed({OakenSiren.class, CopperMyr.class, IcyManipulator.class, GrizzlyBears.class, CaptivatingCave.class})
class OakenSirenTest extends BaseCardTest {

    private void addReadySiren() {
        addCreatureReady(player1, new OakenSiren());
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
        harness.addMana(player1, ManaColor.GREEN, 1);

        activateForBlue();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted blue mana pays the blue symbol of an artifact spell")
    void paysColoredArtifactSpellCost() {
        addReadySiren();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OakenSiren()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateForBlue();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Oaken Siren")).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot pay for a nonartifact mana ability")
    void cannotPayNonartifactActivatedAbility() {
        addReadySiren();
        Permanent cave = harness.addToBattlefieldAndReturn(player1, new CaptivatingCave());

        activateForBlue();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cave.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The mana ability resolves immediately and cannot be used twice while tapped")
    void tapsAsCostWithoutUsingStack() {
        addReadySiren();

        activateForBlue();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Oaken Siren").isTapped()).isTrue();
        assertThatThrownBy(this::activateForBlue).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Oaken Siren cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        Permanent siren = harness.addToBattlefieldAndReturn(player1, new OakenSiren());
        siren.setSummoningSick(true);

        assertThatThrownBy(this::activateForBlue).isInstanceOf(IllegalStateException.class);
        assertThat(siren.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Vigilance lets Oaken Siren attack and then tap for mana")
    void canProduceManaAfterAttacking() {
        Permanent siren = addCreatureReady(player1, new OakenSiren());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(siren.isTapped()).isFalse();
        activateForBlue();
        assertThat(siren.isTapped()).isTrue();
        assertThat(siren.isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Oaken Siren")
    void cannotBeBlockedByGroundCreature() {
        addReadySiren();
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
