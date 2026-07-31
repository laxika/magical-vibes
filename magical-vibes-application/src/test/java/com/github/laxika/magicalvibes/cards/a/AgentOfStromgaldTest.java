package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentOfStromgaldTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: Add {B} converts one red mana into one black mana without using the stack")
    void redManaBecomesBlackMana() {
        harness.addToBattlefield(player1, new AgentOfStromgald());
        harness.addMana(player1, ManaColor.RED, 1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        // Mana ability — never uses the stack (CR 605.3b).
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without red mana available")
    void cannotActivateWithoutRedMana() {
        harness.addToBattlefield(player1, new AgentOfStromgald());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability does not require tapping, so it can be activated repeatedly")
    void repeatableWithoutTapping() {
        harness.addToBattlefield(player1, new AgentOfStromgald());
        harness.addMana(player1, ManaColor.RED, 3);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
