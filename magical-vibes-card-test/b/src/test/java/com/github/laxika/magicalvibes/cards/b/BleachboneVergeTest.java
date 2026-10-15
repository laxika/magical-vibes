package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({BleachboneVerge.class, Plains.class, Swamp.class})
class BleachboneVergeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingForBlackMana() {
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("White mana ability requires a Plains or Swamp")
    void whiteManaRequiresPlainsOrSwamp() {
        Permanent verge = addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Plains or a Swamp");
        assertThat(verge.isTapped()).isFalse();
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
    @DisplayName("Tapping for white mana works while controlling a Swamp")
    void tappingForWhiteManaWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Plains does not enable white mana")
    void opponentsPlainsDoesNotEnableWhiteMana() {
        harness.addToBattlefield(player2, new Plains());
        Permanent verge = addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(verge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Swamp still enables white mana")
    void tappedSwampEnablesWhiteMana() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.tap();
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another Bleachbone Verge does not enable white mana")
    void anotherVergeDoesNotEnableWhiteMana() {
        addReadyVerge(player1);
        Permanent verge = addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Plains or a Swamp");
        assertThat(verge.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("White mana is unavailable after the qualifying land leaves")
    void losingQualifyingLandDisablesWhiteMana() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent verge = addReadyVerge(player1);
        harness.activateAbility(player1, 1, 1, null, null);
        verge.untap();
        gd.playerBattlefields.get(player1.getId()).remove(swamp);
        gd.playerGraveyards.get(player1.getId()).add(swamp.getCard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Plains or a Swamp");
        assertThat(verge.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A newly entered Verge can immediately produce black mana")
    void newlyEnteredVergeCanProduceMana() {
        Permanent verge = harness.addToBattlefieldAndReturn(player1, new BleachboneVerge());
        verge.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyVerge(Player player) {
        return addCreatureReady(player, new BleachboneVerge());
    }
}
