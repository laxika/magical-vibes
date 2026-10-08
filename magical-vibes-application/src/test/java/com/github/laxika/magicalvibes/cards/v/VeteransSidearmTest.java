package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeteransSidearm.class, GrizzlyBears.class})
class VeteransSidearmTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Veteran's Sidearm puts it on the battlefield unattached")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new VeteransSidearm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Veteran's Sidearm") && !p.isAttached());
    }

    @Test
    @DisplayName("Equip {1} attaches the Sidearm to a creature you control")
    void equipAttachesToCreature() {
        Permanent gear = addGearReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gear.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent gear = addGearReady(player1);
        gear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost goes away when the Sidearm leaves the battlefield")
    void boostEndsWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent gear = addGearReady(player1);
        gear.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(gear);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unequipped creatures are unaffected")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addReadyCreature(player1);
        Permanent other = addReadyCreature(player1);
        Permanent gear = addGearReady(player1);
        gear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip fizzles if the target creature leaves before resolution")
    void equipFizzlesIfTargetRemoved() {
        addGearReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Veteran's Sidearm").getAttachedTo()).isNull();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    void reequippingMovesBoostOnlyOnResolutionAndPaysOneMana() {
        Permanent gear = addGearReady(player1);
        Permanent original = addReadyCreature(player1);
        Permanent replacement = addReadyCreature(player1);
        gear.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, replacement.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gear.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gear.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(3);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent gear = addGearReady(player1);
        Permanent creature = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gear.isAttached()).isFalse();
    }

    @Test
    void cannotEquipOutsideMainPhase() {
        addGearReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotEquipDuringOpponentsTurn() {
        addGearReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotEquipWhileStackIsNonempty() {
        addGearReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();
    }

    @Test
    void targetChangingControllerLeavesOriginalAttachmentIntact() {
        Permanent gear = addGearReady(player1);
        Permanent original = addReadyCreature(player1);
        Permanent target = addReadyCreature(player1);
        gear.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gear.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    private Permanent addGearReady(Player player) {
        return addCreatureReady(player, new VeteransSidearm());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
