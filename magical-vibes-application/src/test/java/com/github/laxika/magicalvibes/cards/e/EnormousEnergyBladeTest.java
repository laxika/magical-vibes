package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfOtherworldlyLight;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnormousEnergyBlade.class, GrizzlyBears.class, MarchOfOtherworldlyLight.class})
class EnormousEnergyBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +4/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attaching the Blade taps the equipped creature")
    void attachingBladeTapsEquippedCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Re-equipping the Blade taps the new equipped creature")
    void reEquippingBladeTapsNewEquippedCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent firstCreature = addCreatureReady(player1);
        Permanent secondCreature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, firstCreature.getId());
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, secondCreature.getId());
        resolveAllTriggers();

        assertThat(blade.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
    }

    private Permanent addBladeReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new EnormousEnergyBlade());
    }

    @Test
    @DisplayName("Attachment trigger still taps the creature after the Blade is exiled")
    void attachmentTriggerSurvivesBladeLeavingBattlefield() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isFalse();

        harness.setHand(player2, List.of(new MarchOfOtherworldlyLight()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);
        harness.castInstantForXWithDiscards(player2, 0, 3, List.of(blade.getId()), List.of());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blade);

        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipping the same creature again does not trigger another tap")
    void equippingSameCreatureDoesNotTrigger() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        resolveAllTriggers();
        assertThat(creature.isTapped()).isTrue();
        creature.untap();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        resolveAllTriggers();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(blade.getAttachedTo()).isNull();
        assertThat(creature.isTapped()).isFalse();
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
