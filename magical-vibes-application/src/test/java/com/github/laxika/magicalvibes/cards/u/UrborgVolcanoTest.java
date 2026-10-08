package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrborgVolcano.class})
class UrborgVolcanoTest extends BaseCardTest {

    // ===== Enters the battlefield tapped =====

    @Test
    @DisplayName("Urborg Volcano enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new UrborgVolcano()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent land = findPermanent(player1, "Urborg Volcano");
        assertThat(land.isTapped()).isTrue();
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        addLandReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        addLandReady();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate a second mana ability while tapped")
    void cannotActivateWhileTapped() {
        addLandReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Helper methods =====

    private Permanent addLandReady() {
        return harness.addToBattlefieldAndReturn(player1, new UrborgVolcano());
    }

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new UrborgVolcano()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering through an effect still enters tapped and cannot produce mana")
    void enteringThroughEffectCannotProduceManaImmediately() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new UrborgVolcano());

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already tapped");
        }
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("After untapping, each activation immediately produces only the chosen color")
    void untappingAllowsChoosingEitherColorWithoutUsingStack() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new UrborgVolcano());

        harness.performUntapStep(player1);
        assertThat(land.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
