package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AerialSurveyor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TanukiTransplanter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwiftReconfiguration.class, GrizzlyBears.class, AerialSurveyor.class, TanukiTransplanter.class})
class SwiftReconfigurationTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature becomes an artifact Vehicle")
    void enchantedCreatureBecomesArtifactVehicle() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachAura(bears);

        assertThat(gqs.isArtifact(gd, bears)).isTrue();
        assertThat(gqs.isCreature(gd, bears)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.VEHICLE)).isTrue();
    }

    @Test
    @DisplayName("Granted Crew 5 ability animates the enchanted permanent")
    void enchantedPermanentCanBeCrewed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachAura(bears);

        Permanent firstCrew = addReadyBears();
        Permanent secondCrew = addReadyBears();
        Permanent thirdCrew = addReadyBears();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        assertThat(thirdCrew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature non-Vehicle permanent")
    void cannotTargetOtherPermanent() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SwiftReconfiguration());
        harness.setHand(player1, List.of(new SwiftReconfiguration()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    private void attachAura(Permanent target) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SwiftReconfiguration());
        aura.setAttachedTo(target.getId());
    }

    @Test
    void castAuraRemainsAttachedToOpponentsConvertedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TanukiTransplanter());

        castAura(creature);

        harness.assertOnBattlefield(player1, "Swift Reconfiguration");
        assertThat(gqs.isCreature(gd, creature)).isFalse();
        assertThat(gqs.isArtifact(gd, creature)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.VEHICLE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.EQUIPMENT)).isTrue();
    }

    @Test
    void convertedCreatureLosesCreatureSubtypes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TanukiTransplanter());

        castAura(creature);

        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.DOG)).isFalse();
    }

    @Test
    void existingVehicleRetainsOriginalCrewAbility() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new AerialSurveyor());
        castAura(vehicle);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new TanukiTransplanter());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }

    @Test
    void grantedCrewRejectsInsufficientPower() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new AerialSurveyor());
        castAura(vehicle);
        harness.addToBattlefield(player1, new TanukiTransplanter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");
    }

    @Test
    void convertingAttackerRemovesItFromCombat() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new TanukiTransplanter());
        attacker.setAttacking(true);

        castAura(attacker);

        assertThat(attacker.isAttacking()).isFalse();
    }

    private void castAura(Permanent target) {
        harness.setHand(player1, List.of(new SwiftReconfiguration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        return bears;
    }
}
