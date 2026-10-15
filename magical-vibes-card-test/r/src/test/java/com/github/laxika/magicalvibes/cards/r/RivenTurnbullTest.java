package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RivenTurnbull.class})
class RivenTurnbullTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Riven Turnbull produces one black mana")
    void activatingProducesBlackMana() {
        Permanent rivenTurnbull = addCreatureReady(player1, new RivenTurnbull());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(rivenTurnbull.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Riven Turnbull cannot activate while it has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new RivenTurnbull());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Mana ability resolves immediately without using the stack")
    void manaAbilityResolvesImmediately() {
        addCreatureReady(player1, new RivenTurnbull());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Riven Turnbull cannot produce mana again")
    void cannotActivateAgainWhileTapped() {
        addCreatureReady(player1, new RivenTurnbull());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Riven Turnbull adds mana to its controller's pool")
    void addsManaToControllersPool() {
        addCreatureReady(player2, new RivenTurnbull());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
