package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SunlitMarsh.class)
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

    private void tapFor(ManaColor manaColor, String choice) {
        Permanent marsh = new Permanent(new SunlitMarsh());
        marsh.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(marsh);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, choice);

        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
        assertThat(marsh.isTapped()).isTrue();
    }
}
