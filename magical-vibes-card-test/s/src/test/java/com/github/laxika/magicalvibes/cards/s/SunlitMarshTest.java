package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunlitMarsh.class})
class SunlitMarshTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new SunlitMarsh()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Sunlit Marsh").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingForWhiteMana() {
        tapFor(ManaColor.WHITE, "WHITE");
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingForBlackMana() {
        tapFor(ManaColor.BLACK, "BLACK");
    }

    @Test
    @DisplayName("Cannot activate the mana ability while tapped")
    void cannotProduceManaWhileTapped() {
        harness.setHand(player1, List.of(new SunlitMarsh()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enters tapped when put directly onto the battlefield")
    void entersTappedWithoutBeingPlayed() {
        Permanent marsh = harness.enterBattlefieldAndReturn(player1, new SunlitMarsh());

        assertThat(marsh.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can produce mana after untapping on the turn it enters")
    void canProduceManaAfterUntapping() {
        harness.setHand(player1, List.of(new SunlitMarsh()));
        harness.playLand(player1, 0);
        Permanent marsh = findPermanent(player1, "Sunlit Marsh");
        marsh.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(marsh.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void tapFor(ManaColor manaColor, String choice) {
        Permanent marsh = harness.addToBattlefieldAndReturn(player1, new SunlitMarsh());
        marsh.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, choice);

        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
        assertThat(marsh.isTapped()).isTrue();
    }
}
