package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WastewoodVerge.class, Forest.class, Swamp.class})
class WastewoodVergeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingForGreenMana() {
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Black mana ability requires a Swamp or Forest")
    void blackManaRequiresSwampOrForest() {
        Permanent verge = addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Swamp or a Forest");
        assertThat(verge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for black mana works while controlling a Swamp")
    void tappingForBlackManaWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana works while controlling a Forest")
    void tappingForBlackManaWithForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Swamp does not enable black mana")
    void opponentsSwampDoesNotEnableBlackMana() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent verge = addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(verge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Forest still enables black mana")
    void tappedForestEnablesBlackMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the only qualifying land disables black mana")
    void losingQualifyingLandDisablesBlackMana() {
        Permanent verge = addReadyVerge(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerGraveyards.get(player1.getId()).add(forest.getCard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Swamp or a Forest");
        assertThat(verge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Verge cannot activate its other mana ability")
    void cannotActivateAgainWhileTapped() {
        harness.addToBattlefield(player1, new Forest());
        addReadyVerge(player1);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyVerge(Player player) {
        return harness.addToBattlefieldAndReturn(player, new WastewoodVerge());
    }
}
