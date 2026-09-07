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
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equip attaches Glider Staff to a creature you control")
    void equipAttachesToCreature() {
        Permanent staff = addStaffReady(player1);
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
        harness.passBothPriorities();
        harness.passBothPriorities();

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

    private Permanent addStaffReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent staff = new Permanent(new GliderStaff());
        staff.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(staff);
        return staff;
    }
}
