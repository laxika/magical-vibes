package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SisaysRing.class})
class SisaysRingTest extends BaseCardTest {

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for mana adds two colorless mana")
    void tapForTwoColorlessMana() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SisaysRing());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(ring.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability resolves without using the stack")
    void manaAbilityDoesNotUseStack() {
        harness.addToBattlefield(player1, new SisaysRing());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate the mana ability while tapped")
    void cannotActivateWhileTapped() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SisaysRing());
        ring.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Can tap for mana on the turn it enters the battlefield")
    void canActivateImmediatelyAfterResolving() {
        harness.castFromHand(player1, new SisaysRing(), "{4}");
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana is added only to the activating controller's pool")
    void addsManaOnlyToController() {
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SisaysRing());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(ring.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
