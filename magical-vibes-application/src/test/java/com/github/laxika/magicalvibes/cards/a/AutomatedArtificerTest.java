package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed({AutomatedArtificer.class, CopperMyr.class, GrizzlyBears.class, AdarkarSentinel.class})
class AutomatedArtificerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Automated Artificer adds artifact-spell-or-ability-restricted colorless mana")
    void addsRestrictedColorlessMana() {
        addReadyArtificer();

        harness.activateAbility(player1, 0, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getArtifactSpellOrAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Automated Artificer's mana can pay for an artifact spell")
    void restrictedManaCanPayForArtifactSpell() {
        addReadyArtificer();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new CopperMyr()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Automated Artificer's mana can pay for a nonartifact activated ability")
    void restrictedManaCanPayForNonartifactAbility() {
        addReadyArtificer();
        Permanent sentinel = addCreatureReady(player1, new AdarkarSentinel());

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Automated Artificer's mana cannot pay for a nonartifact spell")
    void restrictedManaCannotPayForNonartifactSpell() {
        addReadyArtificer();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private Permanent addReadyArtificer() {
        return addCreatureReady(player1, new AutomatedArtificer());
    }
}
