package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Savannah.class)
class SavannahTest extends BaseCardTest {

    @Test
    @DisplayName("Savannah produces green mana")
    void producesGreenMana() {
        Permanent savannah = addSavannahReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(savannah.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Savannah produces white mana")
    void producesWhiteMana() {
        Permanent savannah = addSavannahReady();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(savannah.isTapped()).isTrue();
    }

    private Permanent addSavannahReady() {
        Permanent savannah = harness.addToBattlefieldAndReturn(player1, new Savannah());
        savannah.setSummoningSick(false);
        return savannah;
    }
}
