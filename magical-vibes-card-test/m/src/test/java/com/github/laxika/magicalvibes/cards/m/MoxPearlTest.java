package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MoxPearl.class)
class MoxPearlTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mox Pearl adds one white mana")
    void tappingAddsWhiteMana() {
        Permanent mox = harness.addToBattlefieldAndReturn(player1, new MoxPearl());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(mox.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mox Pearl can produce mana on the turn it enters the battlefield")
    void canActivateImmediatelyAfterEntering() {
        harness.setHand(player1, List.of(new MoxPearl()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Mox Pearl cannot produce mana again")
    void cannotActivateWhileTapped() {
        Permanent mox = harness.addToBattlefieldAndReturn(player1, new MoxPearl());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(mox.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
