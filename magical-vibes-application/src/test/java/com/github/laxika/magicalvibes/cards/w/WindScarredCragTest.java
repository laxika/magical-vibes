package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindScarredCrag.class})
class WindScarredCragTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield tapped gains 1 life")
    void entersTappedAndGainsOneLife() {
        harness.setHand(player1, List.of(new WindScarredCrag()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent crag = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(crag.isTapped()).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        tapFor(ManaColor.RED);
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        tapFor(ManaColor.WHITE);
    }

    @Test
    @DisplayName("Life gain waits for the entry trigger to resolve and benefits only the controller")
    void lifeGainUsesTheStack() {
        harness.setHand(player1, List.of(new WindScarredCrag()));
        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A tapped Crag cannot pay its mana ability's tap cost")
    void cannotActivateWhileTapped() {
        Permanent crag = harness.addToBattlefieldAndReturn(player1, new WindScarredCrag());
        crag.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    private void tapFor(ManaColor color) {
        Permanent crag = harness.addToBattlefieldAndReturn(player1, new WindScarredCrag());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        ManaColor otherColor = color == ManaColor.RED ? ManaColor.WHITE : ManaColor.RED;
        assertThat(gd.playerManaPools.get(player1.getId()).get(otherColor)).isZero();
        assertThat(crag.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
