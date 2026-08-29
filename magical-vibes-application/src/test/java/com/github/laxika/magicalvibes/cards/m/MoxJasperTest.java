package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
