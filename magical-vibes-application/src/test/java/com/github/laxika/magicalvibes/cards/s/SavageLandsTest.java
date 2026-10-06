package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavageLands.class})
class SavageLandsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new SavageLands()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        addLandReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        addLandReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        addLandReady(player1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering without being played still enters tapped")
    void entersTappedWithoutBeingPlayed() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new SavageLands());

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped land cannot produce mana")
    void tappedLandCannotProduceMana() {
        harness.enterBattlefieldAndReturn(player1, new SavageLands());

        for (int abilityIndex = 0; abilityIndex < 3; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana resolves immediately and tapping prevents a second activation")
    void manaResolvesImmediatelyAndCannotBeProducedTwice() {
        addLandReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private void addLandReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SavageLands());
        perm.setSummoningSick(false);
    }
}
