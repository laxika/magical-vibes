package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncientDen.class})
class AncientDenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds {W}")
    void tapForWhiteMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AncientDen());

        harness.activateAbility(player1, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ancient Den can produce mana immediately after being played")
    void canProduceManaOnTheTurnItEnters() {
        harness.setHand(player1, List.of(new AncientDen()));

        harness.playLand(player1, 0);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Ancient Den cannot produce additional mana")
    void cannotActivateAgainWhileTapped() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AncientDen());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }
}
