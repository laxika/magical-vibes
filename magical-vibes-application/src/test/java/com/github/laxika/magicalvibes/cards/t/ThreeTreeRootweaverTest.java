package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ThreeTreeRootweaver.class)
class ThreeTreeRootweaverTest extends BaseCardTest {

    @Test
    void tappingForManaPromptsForAColorAndAddsOneMana() {
        Permanent rootweaver = harness.addToBattlefieldAndReturn(player1, new ThreeTreeRootweaver());
        rootweaver.setSummoningSick(false);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        assertThat(rootweaver.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(before + 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
