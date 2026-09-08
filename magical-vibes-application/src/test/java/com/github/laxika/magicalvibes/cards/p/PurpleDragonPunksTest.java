package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AmbassadorLaquatus;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({PurpleDragonPunks.class, CopperMyr.class, GrizzlyBears.class, AmbassadorLaquatus.class})
class PurpleDragonPunksTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Purple Dragon Punks adds red artifact-spell-or-ability-restricted mana")
    void addsRestrictedRedMana() {
        addReadyPunks();

        harness.activateAbility(player1, 0, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getArtifactSpellOrAbilityOnlyMana(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Restricted mana pays for an artifact spell")
    void paysForArtifactSpell() {
        addReadyPunks();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new CopperMyr()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOrAbilityOnlyMana(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Restricted mana pays for a nonartifact ability")
    void paysForNonartifactAbility() {
        addReadyPunks();
        harness.addToBattlefield(player1, new AmbassadorLaquatus());

        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOrAbilityOnlyMana(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot pay for a nonartifact spell")
    void cannotPayForNonartifactSpell() {
        addReadyPunks();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOrAbilityOnlyMana(ManaColor.RED)).isEqualTo(1);
    }

    private void addReadyPunks() {
        harness.addToBattlefield(player1, new PurpleDragonPunks());
        Permanent punks = findPermanent(player1, "Purple Dragon Punks");
        punks.setSummoningSick(false);
    }
}
