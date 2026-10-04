package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GliderStaff.class, GrizzlyBears.class, Island.class})
class GliderStaffTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 and flying")
    void equippedCreatureGetsBoostAndFlying() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addCreatureReady(player1, new GliderStaff());
        staff.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equip attaches Glider Staff to a creature you control")
    void equipAttachesToCreature() {
        Permanent staff = addCreatureReady(player1, new GliderStaff());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(staff.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Entering Glider Staff airbends up to one target creature")
    void enterTheBattlefieldAirbendsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GliderStaff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(creature.getOriginalCard().getId()))
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Glider Staff cannot airbend a land")
    void cannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new GliderStaff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void entersWithoutChoosingATargetOnAnEmptyBattlefield() {
        harness.setHand(player1, List.of(new GliderStaff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Glider Staff");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void airbendsOwnCreatureAndAllowsCastingItForTwoGenericMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GliderStaff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, creature.getOriginalCard().getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNull();
    }

    @Test
    void movingEquipmentTransfersBothBonuses() {
        Permanent staff = addCreatureReady(player1, new GliderStaff());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        staff.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        resolveAllTriggers();

        assertThat(staff.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotEquipAnOpponentsCreature() {
        Permanent staff = addCreatureReady(player1, new GliderStaff());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(staff.getAttachedTo()).isNull();
    }
}
