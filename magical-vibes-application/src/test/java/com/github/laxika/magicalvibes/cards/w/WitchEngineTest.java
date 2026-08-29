package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WitchEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Adds four black mana and gives Witch Engine to the target opponent")
    void addsManaAndGivesControlToTargetOpponent() {
        Permanent witchEngine = addCreatureReady(player1, new WitchEngine());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(witchEngine.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Witch Engine");
        harness.assertNotOnBattlefield(player1, "Witch Engine");
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        addCreatureReady(player1, new WitchEngine());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
