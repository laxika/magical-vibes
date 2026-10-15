package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KillerWhale;
import com.github.laxika.magicalvibes.cards.m.ManaLeak;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HydraulicHelper.class, CopperMyr.class, KillerWhale.class, GrizzlyBears.class, ManaLeak.class})
class HydraulicHelperTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Hydraulic Helper adds blue artifact-spell-or-ability-restricted mana")
    void addsRestrictedBlueMana() {
        addReadyHelper();

        harness.activateAbility(player1, 0, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Hydraulic Helper mana can pay for an artifact spell")
    void paysForArtifactSpell() {
        addReadyHelper();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new CopperMyr()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Hydraulic Helper mana can pay for a nonartifact ability")
    void paysForNonartifactAbility() {
        addReadyHelper();
        Permanent whale = harness.addToBattlefieldAndReturn(player1, new KillerWhale());

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, whale, Keyword.FLYING)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Hydraulic Helper mana cannot pay for a nonartifact spell")
    void cannotPayForNonartifactSpell() {
        addReadyHelper();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @CardUsed({HydraulicHelper.class, GrizzlyBears.class, ManaLeak.class})
    @DisplayName("Hydraulic Helper mana can pay Mana Leak's cost during resolution")
    void paysCostDuringSpellResolution() {
        addReadyHelper();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Hydraulic Helper's mana ability resolves immediately and taps its source")
    void manaAbilityDoesNotUseStack() {
        addReadyHelper();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Hydraulic Helper").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("A summoning-sick Hydraulic Helper cannot activate its tap ability")
    void summoningSicknessPreventsManaAbility() {
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new HydraulicHelper());
        helper.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(helper.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isZero();
    }

    private void addReadyHelper() {
        harness.addToBattlefieldAndReturn(player1, new HydraulicHelper()).setSummoningSick(false);
    }
}
