package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SetonKrosanProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an untapped Druid adds green mana")
    void tapsDruidForGreenMana() {
        Permanent seton = addCreatureReady(player1, new SetonKrosanProtector());

        harness.activateAbility(player1, 0, null, null);

        assertThat(seton.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only an untapped Druid the controller controls can be tapped")
    void ignoresNonDruidsAndOpponents() {
        Permanent seton = addCreatureReady(player1, new SetonKrosanProtector());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentSeton = addCreatureReady(player2, new SetonKrosanProtector());

        harness.activateAbility(player1, 0, null, null);

        assertThat(seton.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
        assertThat(opponentSeton.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without an untapped Druid")
    void cannotActivateWithoutUntappedDruid() {
        Permanent seton = addCreatureReady(player1, new SetonKrosanProtector());
        seton.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }
}
