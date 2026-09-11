package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SkyshroudTroopers.class)
class SkyshroudTroopersTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {G} produces one green mana")
    void tapAddsGreen() {
        Permanent troopers = addCreatureReady(player1, new SkyshroudTroopers());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(troopers.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the mana ability while summoning sick")
    void summoningSickCannotActivate() {
        Permanent troopers = harness.addToBattlefieldAndReturn(player1, new SkyshroudTroopers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(troopers.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the mana ability again while tapped")
    void tappedCannotActivateAgain() {
        Permanent troopers = addCreatureReady(player1, new SkyshroudTroopers());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(troopers.isTapped()).isTrue();
    }
}
