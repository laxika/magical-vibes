package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParadiseMantleTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature can tap for a chosen color")
    void equippedCreatureCanTapForChosenColor() {
        Permanent creature = addCreatureReady(player1);
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equip {1} attaches Paradise Mantle to a creature you control")
    void equipAttachesToCreature() {
        Permanent mantle = addMantleReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mantle.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("An unattached creature does not have Paradise Mantle's mana ability")
    void unattachedCreatureDoesNotHaveManaAbility() {
        addCreatureReady(player1);
        addMantleReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addMantleReady(Player player) {
        Permanent perm = new Permanent(new ParadiseMantle());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
