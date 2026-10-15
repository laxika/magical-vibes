package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeashellCameo.class})
class SeashellCameoTest extends BaseCardTest {

    @Test
    @DisplayName("Seashell Cameo can produce mana on the turn it enters")
    void canProduceManaOnTheTurnItEnters() {
        harness.castFromHand(player1, new SeashellCameo(), "{3}");
        harness.passBothPriorities();

        Permanent cameo = findPermanent(player1, "Seashell Cameo");
        assertThat(cameo.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(cameo.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Seashell Cameo cannot produce more mana")
    void cannotActivateAgainWhileTapped() {
        harness.addToBattlefield(player1, new SeashellCameo());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping Seashell Cameo adds one blue mana")
    void tappingAddsBlueMana() {
        Permanent cameo = harness.addToBattlefieldAndReturn(player1, new SeashellCameo());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(cameo.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping Seashell Cameo adds one white mana")
    void tappingAddsWhiteMana() {
        Permanent cameo = harness.addToBattlefieldAndReturn(player1, new SeashellCameo());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(cameo.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
