package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import java.util.List;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(OrnithopterOfParadise.class)
class OrnithopterOfParadiseTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for one mana of any color")
    void tapsForAnyColor() {
        Permanent ornithopter = addCreatureReady(player1, new OrnithopterOfParadise());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(ornithopter.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, mode = EnumSource.Mode.EXCLUDE, names = "COLORLESS")
    void producesExactlyOneManaOfChosenColor(ManaColor color) {
        Permanent ornithopter = addCreatureReady(player1, new OrnithopterOfParadise());
        var pool = gd.playerManaPools.get(player1.getId());
        int before = pool.get(color);
        int totalBefore = pool.getTotalAllMana();
        int opponentBefore = gd.playerManaPools.get(player2.getId()).getTotalAllMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, color.name());

        assertThat(pool.get(color)).isEqualTo(before + 1);
        assertThat(pool.getTotalAllMana()).isEqualTo(totalBefore + 1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(opponentBefore);
        assertThat(ornithopter.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new OrnithopterOfParadise());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        addCreatureReady(player1, new OrnithopterOfParadise());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        int manaBefore = gd.playerManaPools.get(player1.getId()).getTotalAllMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(manaBefore);
    }

    @Test
    void cannotChooseColorlessMana() {
        addCreatureReady(player1, new OrnithopterOfParadise());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.COLORLESS.name()))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, ManaColor.WHITE.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void flyingCreatureCanBlockIt() {
        addCreatureReady(player1, new OrnithopterOfParadise());
        Permanent blocker = addCreatureReady(player2, new OrnithopterOfParadise());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
