package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DimensionX.class)
class DimensionXTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DimensionX()));

        harness.playLand(player1, 0);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Mana ability adds red mana when red is chosen")
    void manaAbilityAddsRedMana() {
        tapFor(ManaColor.RED);
    }

    @Test
    @DisplayName("Mana ability adds white mana when white is chosen")
    void manaAbilityAddsWhiteMana() {
        tapFor(ManaColor.WHITE);
    }

    private void tapFor(ManaColor color) {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    private Permanent addReadyLand() {
        Permanent land = new Permanent(new DimensionX());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(land);
        return land;
    }
}
