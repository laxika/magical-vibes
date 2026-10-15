package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Memnite;
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

@CardUsed({GruesomeSlaughter.class, Memnite.class, GrizzlyBears.class})
class GruesomeSlaughterTest extends BaseCardTest {

    @Test
    @DisplayName("Colorless creatures you control can tap to deal damage equal to their power")
    void colorlessCreaturesGainPowerDamageAbility() {
        Permanent memnite = addCreatureReady(player1, new Memnite());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castGruesomeSlaughter();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(memnite.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Colored creatures do not gain the granted ability")
    void coloredCreaturesDoNotGainAbility() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castGruesomeSlaughter();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("The granted ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        addCreatureReady(player1, new Memnite());

        castGruesomeSlaughter();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Opposing colorless creatures do not gain the ability")
    void opposingColorlessCreaturesDoNotGainAbility() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new Memnite());
        castGruesomeSlaughter();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain the ability")
    void laterCreaturesDoNotGainAbility() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castGruesomeSlaughter();
        addCreatureReady(player1, new Memnite());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Damage uses the creature's power at ability resolution")
    void damageUsesPowerAtResolution() {
        Permanent source = addCreatureReady(player1, new Memnite());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castGruesomeSlaughter();

        harness.activateAbility(player1, 0, null, target.getId());
        source.setPowerModifier(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature with zero power deals no damage")
    void zeroPowerDealsNoDamage() {
        Permanent source = addCreatureReady(player1, new Memnite());
        source.setPowerModifier(-1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castGruesomeSlaughter();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The granted ability can target a creature you control")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new Memnite());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castGruesomeSlaughter();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The granted tap ability respects summoning sickness")
    void summoningSicknessPreventsActivation() {
        Permanent source = addCreatureReady(player1, new Memnite());
        source.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castGruesomeSlaughter();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The granted ability remains available during the end step")
    void grantedAbilityRemainsDuringEndStep() {
        addCreatureReady(player1, new Memnite());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castGruesomeSlaughter();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    private void castGruesomeSlaughter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GruesomeSlaughter()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveInstant(player1, 0);
    }
}
