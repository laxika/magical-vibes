package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SabertoothNishobaTest extends BaseCardTest {

    @Test
    void hasProtectionFromBlueAndRedButNotGreen() {
        harness.addToBattlefield(player1, new SabertoothNishoba());

        Permanent nishoba = findPermanent(player1, "Sabertooth Nishoba");

        assertThat(gqs.hasProtectionFrom(gd, nishoba, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, nishoba, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, nishoba, CardColor.GREEN)).isFalse();
    }
}
