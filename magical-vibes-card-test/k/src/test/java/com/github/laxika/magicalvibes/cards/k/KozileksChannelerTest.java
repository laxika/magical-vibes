package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KozileksChanneler.class})
class KozileksChannelerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Kozilek's Channeler adds two colorless mana")
    void tappingAddsTwoColorlessMana() {
        Permanent channeler = addCreatureReady(player1, new KozileksChanneler());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(channeler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability resolves immediately without using the stack")
    void manaAbilityResolvesImmediately() {
        addCreatureReady(player1, new KozileksChanneler());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Summoning-sick Channeler cannot pay its tap cost")
    void summoningSickCannotActivate() {
        Permanent channeler = harness.addToBattlefieldAndReturn(player1, new KozileksChanneler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(channeler.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Channeler cannot activate again while tapped")
    void cannotActivateAgainWhileTapped() {
        addCreatureReady(player1, new KozileksChanneler());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Channeler adds mana to its activating controller's pool")
    void addsManaToActivatingControllersPool() {
        Permanent channeler = addCreatureReady(player2, new KozileksChanneler());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(channeler.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
