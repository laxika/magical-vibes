package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.d.DocksideChef;
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

@CardUsed({AutomatedArtificer.class, CopperMyr.class, GrizzlyBears.class, DocksideChef.class})
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
        harness.addToBattlefield(player1, new DocksideChef());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AutomatedArtificer()));

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Dockside Chef"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dockside Chef");
        harness.assertInHand(player1, "Automated Artificer");
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

    @Test
    @DisplayName("The mana ability resolves immediately and taps its source")
    void manaAbilityDoesNotUseStack() {
        Permanent artificer = addReadyArtificer();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(artificer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Automated Artificer cannot activate its tap ability")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new AutomatedArtificer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("An already-tapped Automated Artificer cannot produce more mana")
    void cannotActivateTwiceWithoutUntapping() {
        addReadyArtificer();
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private Permanent addReadyArtificer() {
        return addCreatureReady(player1, new AutomatedArtificer());
    }
}
