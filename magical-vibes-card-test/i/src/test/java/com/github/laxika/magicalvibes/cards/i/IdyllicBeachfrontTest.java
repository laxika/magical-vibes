package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IdyllicBeachfront.class})
class IdyllicBeachfrontTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new IdyllicBeachfront()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Idyllic Beachfront").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingForWhiteProducesMana() {
        tapFor(ManaColor.WHITE, 0);
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingForBlueProducesMana() {
        tapFor(ManaColor.BLUE, 1);
    }

    @Test
    @DisplayName("Neither mana ability can be activated while the land is tapped")
    void cannotProduceManaWhileTapped() {
        harness.setHand(player1, List.of(new IdyllicBeachfront()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("tapped");
        }

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("After untapping, the land can produce either color immediately")
    void canProduceEachColorAfterUntapping() {
        harness.setHand(player1, List.of(new IdyllicBeachfront()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
        Permanent land = findPermanent(player1, "Idyllic Beachfront");

        harness.performUntapStep(player1);
        assertThat(land.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void tapFor(ManaColor color, int abilityIndex) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new IdyllicBeachfront());
        land.setSummoningSick(false);

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }
}
