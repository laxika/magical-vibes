package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanHellkite;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HargildeKindlyRunechanter.class, CopperMyr.class, ShivanHellkite.class, GrizzlyBears.class, MindStone.class})
class HargildeKindlyRunechanterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Hargilde adds two artifact-restricted colorless mana")
    void addsArtifactRestrictedMana() {
        addReadyHargilde();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hargilde's mana can pay for an artifact spell")
    void artifactRestrictedManaCanPayForArtifactSpell() {
        addReadyHargilde();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CopperMyr()));

        harness.activateAbility(player1, 0, null, null);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
    }

    @Test
    @DisplayName("Hargilde's mana cannot pay for a nonartifact spell")
    void artifactRestrictedManaCannotPayForNonartifactSpell() {
        addReadyHargilde();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hargilde's mana can pay for an artifact ability")
    void artifactRestrictedManaCanPayForArtifactAbility() {
        addReadyHargilde();
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        mindStone.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Hargilde's mana ability resolves immediately and pays its tap cost")
    void manaAbilityResolvesImmediatelyAndCannotBeRepeatedWhileTapped() {
        addReadyHargilde();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Hargilde, Kindly Runechanter").isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hargilde cannot activate its tap ability while summoning sick")
    void summoningSicknessPreventsManaAbility() {
        Permanent hargilde = harness.addToBattlefieldAndReturn(player1, new HargildeKindlyRunechanter());
        hargilde.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hargilde.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
    }

    @Test
    @DisplayName("Hargilde's mana cannot pay the generic cost of a nonartifact ability")
    void artifactRestrictedManaCannotPayForNonartifactAbility() {
        addReadyHargilde();
        harness.addToBattlefield(player1, new ShivanHellkite());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private void addReadyHargilde() {
        harness.addToBattlefieldAndReturn(player1, new HargildeKindlyRunechanter()).setSummoningSick(false);
    }
}
