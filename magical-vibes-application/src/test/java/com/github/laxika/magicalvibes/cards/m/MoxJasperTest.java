package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoxJasper.class, DragonWhelp.class})
class MoxJasperTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate without controlling a Dragon")
    void cannotActivateWithoutDragon() {
        harness.addToBattlefield(player1, new MoxJasper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Dragon");
    }

    @Test
    @DisplayName("Opponent's Dragon does not satisfy the activation condition")
    void opponentDragonDoesNotSatisfyCondition() {
        harness.addToBattlefield(player1, new MoxJasper());
        harness.addToBattlefield(player2, new DragonWhelp());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Dragon");
    }

    @Test
    @DisplayName("Controlling a Dragon allows choosing a color for the mana")
    void controllingDragonAllowsManaAbility() {
        harness.addToBattlefield(player1, new MoxJasper());
        harness.addToBattlefield(player1, new DragonWhelp());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("A tapped Dragon allows immediate mana of each color and Jasper pays its tap cost")
    void addsEachColorImmediatelyAndTaps(ManaColor color) {
        var jasper = harness.addToBattlefieldAndReturn(player1, new MoxJasper());
        var dragon = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        dragon.setTapped(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(jasper.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dragon cards in hand and graveyard do not allow activation")
    void dragonCardsOutsideBattlefieldDoNotSatisfyCondition() {
        var jasper = harness.addToBattlefieldAndReturn(player1, new MoxJasper());
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.setGraveyard(player1, List.of(new DragonWhelp()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Dragon");
        assertThat(jasper.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
