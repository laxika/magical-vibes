package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Plateau.class)
class PlateauTest extends BaseCardTest {

    @Test
    @DisplayName("Plateau produces red mana")
    void producesRedMana() {
        Permanent plateau = addPlateauReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(plateau.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Plateau produces white mana")
    void producesWhiteMana() {
        Permanent plateau = addPlateauReady();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(plateau.isTapped()).isTrue();
    }

    private Permanent addPlateauReady() {
        Permanent plateau = new Permanent(new Plateau());
        plateau.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(plateau);
        return plateau;
    }
}
