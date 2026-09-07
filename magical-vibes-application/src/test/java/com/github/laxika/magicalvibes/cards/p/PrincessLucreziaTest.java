package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PrincessLucrezia.class)
class PrincessLucreziaTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Princess Lucrezia adds one blue mana")
    void tappingAddsBlueMana() {
        Permanent princess = addCreatureReady(player1, new PrincessLucrezia());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(princess.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Princess Lucrezia cannot tap for mana while summoning sick")
    void summoningSickCannotActivate() {
        harness.addToBattlefield(player1, new PrincessLucrezia());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }
}
