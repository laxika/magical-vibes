package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent vault = addReadyVault(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping produces black mana")
    void tappingProducesBlackMana() {
        Permanent vault = addReadyVault(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    private void playLand() {
        harness.setHand(player1, List.of(new VaultOfChampions()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyVault(Player player) {
        Permanent vault = new Permanent(new VaultOfChampions());
        vault.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(vault);
        return vault;
    }

    private Permanent vault() {
        return findPermanent(player1, "Vault of Champions");
    }
}
