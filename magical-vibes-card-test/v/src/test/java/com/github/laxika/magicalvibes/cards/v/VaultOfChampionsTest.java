package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(VaultOfChampions.class)
class VaultOfChampionsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with fewer than two opponents")
    void entersTappedWithFewerThanTwoOpponents() {
        playLand();

        assertThat(vault().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped with two or more opponents")
    void entersUntappedWithTwoOrMoreOpponents() {
        gd.orderedPlayerIds.add(UUID.randomUUID());

        playLand();

        assertThat(vault().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping produces white mana")
    void tappingProducesWhiteMana() {
        Permanent vault = addCreatureReady(player1, new VaultOfChampions());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping produces black mana")
    void tappingProducesBlackMana() {
        Permanent vault = addCreatureReady(player1, new VaultOfChampions());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters untapped with three opponents")
    void entersUntappedWithThreeOpponents() {
        gd.orderedPlayerIds.add(UUID.randomUUID());
        gd.orderedPlayerIds.add(UUID.randomUUID());

        playLand();

        assertThat(vault().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Checks opponents when entering without being played")
    void entersTappedWithoutBeingPlayed() {
        Permanent vault = harness.enterBattlefieldAndReturn(player2, new VaultOfChampions());

        assertThat(vault.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped for either controller with two opponents")
    void entersUntappedForOtherController() {
        gd.orderedPlayerIds.add(UUID.randomUUID());

        Permanent vault = harness.enterBattlefieldAndReturn(player2, new VaultOfChampions());

        assertThat(vault.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can produce mana immediately after entering untapped")
    void canProduceManaImmediately() {
        gd.orderedPlayerIds.add(UUID.randomUUID());
        playLand();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(vault().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    private void playLand() {
        harness.setHand(player1, List.of(new VaultOfChampions()));
        harness.playLand(player1, 0);
    }

    private Permanent vault() {
        return findPermanent(player1, "Vault of Champions");
    }
}
