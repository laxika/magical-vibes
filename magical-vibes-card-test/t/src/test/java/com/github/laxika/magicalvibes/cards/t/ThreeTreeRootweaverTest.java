package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThreeTreeRootweaver.class})
class ThreeTreeRootweaverTest extends BaseCardTest {

    @Test
    void tappingForManaPromptsForAColorAndAddsOneMana() {
        Permanent rootweaver = addCreatureReady(player1, new ThreeTreeRootweaver());

        harness.activateAbility(player1, 0, null, null);

        assertThat(rootweaver.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(before + 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void producesExactlyOneManaOfEachAvailableColor(ManaColor color) {
        Permanent rootweaver = addCreatureReady(player1, new ThreeTreeRootweaver());
        int before = gd.playerManaPools.get(player1.getId()).get(color);
        int totalBefore = gd.playerManaPools.get(player1.getId()).getTotal();
        int opponentBefore = gd.playerManaPools.get(player2.getId()).getTotal();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(rootweaver.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(before + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(totalBefore + 1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(opponentBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent rootweaver = harness.addToBattlefieldAndReturn(player1, new ThreeTreeRootweaver());
        rootweaver.setSummoningSick(true);
        int before = gd.playerManaPools.get(player1.getId()).getTotal();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(rootweaver.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(before);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateAgainWithoutUntapping() {
        Permanent rootweaver = addCreatureReady(player1, new ThreeTreeRootweaver());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        int before = gd.playerManaPools.get(player1.getId()).getTotal();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(rootweaver.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(before);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
