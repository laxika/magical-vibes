package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HargildeKindlyRunechanter.class, CopperMyr.class, GrizzlyBears.class, MindStone.class})
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

    private void addReadyHargilde() {
        harness.addToBattlefield(player1, new HargildeKindlyRunechanter());
        findPermanent(player1, "Hargilde, Kindly Runechanter").setSummoningSick(false);
    }
}
