package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Levitation;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColossusHammer.class, AzureDrake.class, GrizzlyBears.class, Levitation.class})
class ColossusHammerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Colossus Hammer to the target creature")
    void resolvingEquipAttaches() {
        Permanent hammer = addHammerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +10/+10 and loses flying")
    void equippedCreatureGetsBoostAndLosesFlying() {
        Permanent creature = addReadyFlyingCreature(player1);
        Permanent hammer = addHammerReady(player1);
        hammer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(14);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Removing Colossus Hammer removes its effects from the creature")
    void removingHammerRemovesItsEffects() {
        Permanent creature = addReadyFlyingCreature(player1);
        Permanent hammer = addHammerReady(player1);
        hammer.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(hammer);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Re-equipping Colossus Hammer transfers its effects")
    void reEquipTransfersEffects() {
        Permanent hammer = addHammerReady(player1);
        Permanent flyingCreature = addReadyFlyingCreature(player1);
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        hammer.setAttachedTo(flyingCreature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, null, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, flyingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flyingCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, flyingCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(12);
    }

    private Permanent addHammerReady(Player player) {
        return addCreatureReady(player, new ColossusHammer());
    }

    private Permanent addReadyFlyingCreature(Player player) {
        return addCreatureReady(player, new AzureDrake());
    }

    @Test
    void equipPaysEightGenericManaAndWaitsForResolution() {
        Permanent hammer = addHammerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 9);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(hammer.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(hammer.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(12);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent hammer = addHammerReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(hammer.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(8);
    }

    @Test
    void cannotEquipDuringCombat() {
        addHammerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void failedReEquipLeavesOriginalCreatureEquipped() {
        Permanent hammer = addHammerReady(player1);
        Permanent original = addReadyFlyingCreature(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        hammer.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(12);
        assertThat(gqs.hasKeyword(gd, original, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void laterFlyingGrantAppliesUntilHammerMovesToAnotherCreature() {
        Permanent hammer = addHammerReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 16);
        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();

        harness.enterBattlefieldAndReturn(player1, new Levitation());

        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(12);
    }
}
