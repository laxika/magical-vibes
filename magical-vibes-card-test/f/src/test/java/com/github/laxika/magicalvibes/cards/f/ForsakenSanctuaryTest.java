package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForsakenSanctuary.class})
class ForsakenSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ForsakenSanctuary()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Forsaken Sanctuary").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one white mana")
    void tapAddsWhiteMana() {
        addCreatureReady(player1, new ForsakenSanctuary());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability adds one black mana")
    void tapAddsBlackMana() {
        addCreatureReady(player1, new ForsakenSanctuary());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters tapped when put directly onto the battlefield")
    void entersTappedWithoutBeingPlayed() {
        Permanent permanent = harness.enterBattlefieldAndReturn(player1, new ForsakenSanctuary());

        assertThat(permanent.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Neither mana ability can be activated while tapped")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new ForsakenSanctuary()));
        harness.playLand(player1, 0);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("tapped");
        }

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("An untapped newly controlled land produces only the selected color immediately")
    void manaAbilityTapsLandAndResolvesImmediately() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new ForsakenSanctuary());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(permanent.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }
}
