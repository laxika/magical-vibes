package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(LotusGuardian.class)
class LotusGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Lotus Guardian adds one mana of the chosen color and taps")
    void manaAbilityAddsChosenColor() {
        Permanent guardian = addCreatureReady(player1, new LotusGuardian());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(guardian.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lotus Guardian can add one mana of each color")
    void manaAbilityAddsEachColor() {
        for (ManaColor color : ManaColor.COLORS) {
            Permanent guardian = addCreatureReady(player1, new LotusGuardian());
            int guardianIndex = gd.playerBattlefields.get(player1.getId()).indexOf(guardian);

            harness.activateAbility(player1, guardianIndex, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            assertThat(guardian.isTapped()).isTrue();
        }
    }

    @Test
    @DisplayName("Cannot activate Lotus Guardian while it has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new LotusGuardian());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }
}
