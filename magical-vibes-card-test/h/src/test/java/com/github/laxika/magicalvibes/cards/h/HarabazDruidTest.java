package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.l.LeatherbackBaloth;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarabazDruid.class, LeatherbackBaloth.class})
class HarabazDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Adds mana equal to the Allies controlled in one chosen color")
    void addsManaEqualToControlledAllies() {
        Permanent druid = addReadyDruid(player1);
        addReadyDruid(player1);
        harness.addToBattlefield(player1, new LeatherbackBaloth());
        addReadyDruid(player2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void loneDruidCanProduceEachColor(ManaColor color) {
        addReadyDruid(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        for (ManaColor candidate : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(candidate))
                    .isEqualTo(candidate == color ? 1 : 0);
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void countsTappedAndSummoningSickAllies() {
        addReadyDruid(player1);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new HarabazDruid());
        other.setSummoningSick(true);
        other.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new HarabazDruid());
        druid.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(druid.isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        addReadyDruid(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addReadyDruid(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new HarabazDruid());
    }
}
