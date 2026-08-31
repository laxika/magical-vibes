package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(RootriderFaun.class)
class RootriderFaunTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Rootrider Faun produces one green mana")
    void tappingProducesGreenMana() {
        Permanent faun = addReadyFaun(player1);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(faun.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {1} and tapping Rootrider Faun produces a chosen color")
    void paidAbilityProducesChosenColor() {
        Permanent faun = addReadyFaun(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(faun.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rootrider Faun's paid ability requires one mana")
    void paidAbilityRequiresMana() {
        addReadyFaun(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyFaun(Player player) {
        Permanent permanent = new Permanent(new RootriderFaun());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
