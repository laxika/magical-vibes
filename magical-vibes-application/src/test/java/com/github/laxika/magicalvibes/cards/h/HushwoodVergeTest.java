package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HushwoodVerge.class, Forest.class, Plains.class})
class HushwoodVergeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingForGreenMana() {
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("White mana ability requires a Forest or Plains")
    void whiteManaRequiresForestOrPlains() {
        Permanent verge = addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Forest or a Plains");
        assertThat(verge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for white mana works while controlling a Forest")
    void tappingForWhiteManaWithForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana works while controlling a Plains")
    void tappingForWhiteManaWithPlains() {
        harness.addToBattlefield(player1, new Plains());
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Forest does not enable white mana")
    void opponentsForestDoesNotEnableWhiteMana() {
        harness.addToBattlefield(player2, new Forest());
        Permanent verge = addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(verge.isTapped()).isFalse();
    }

    private Permanent addReadyVerge(Player player) {
        Permanent verge = new Permanent(new HushwoodVerge());
        verge.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(verge);
        return verge;
    }
}
