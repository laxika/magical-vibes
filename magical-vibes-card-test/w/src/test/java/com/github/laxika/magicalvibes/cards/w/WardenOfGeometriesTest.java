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

@CardUsed(WardenOfGeometries.class)
class WardenOfGeometriesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Warden of Geometries produces one colorless mana")
    void tappingProducesColorlessMana() {
        Permanent warden = addCreatureReady(player1, new WardenOfGeometries());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(warden.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick Warden cannot tap for mana")
    void summoningSicknessPreventsManaActivation() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfGeometries());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(warden.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("An already tapped Warden cannot produce mana again")
    void cannotActivateTwiceWithoutUntapping() {
        Permanent warden = addCreatureReady(player1, new WardenOfGeometries());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(warden.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vigilance leaves an attacking Warden available to tap for mana")
    void attackingWardenCanStillProduceMana() {
        Permanent warden = addCreatureReady(player1, new WardenOfGeometries());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(warden.isAttacking()).isTrue();
        assertThat(warden.isTapped()).isFalse();

        harness.tapPermanent(player1, 0);

        assertThat(warden.isTapped()).isTrue();
        assertThat(warden.isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
