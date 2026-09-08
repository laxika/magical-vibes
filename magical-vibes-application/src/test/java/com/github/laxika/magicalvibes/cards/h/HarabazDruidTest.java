package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HarabazDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Adds mana equal to the Allies controlled in one chosen color")
    void addsManaEqualToControlledAllies() {
        Permanent druid = addReadyDruid(player1);
        addReadyDruid(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        addReadyDruid(player2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private Permanent addReadyDruid(com.github.laxika.magicalvibes.model.Player player) {
        Permanent druid = harness.addToBattlefieldAndReturn(player, new HarabazDruid());
        druid.setSummoningSick(false);
        return druid;
    }
}
