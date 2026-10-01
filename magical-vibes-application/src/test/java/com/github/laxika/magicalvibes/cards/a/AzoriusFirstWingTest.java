package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Dovescape;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AzoriusFirstWing.class, Dovescape.class, AzoriusHerald.class})
class AzoriusFirstWingTest extends BaseCardTest {

    @Test
    @DisplayName("Azorius First-Wing has protection from enchantments")
    void hasProtectionFromEnchantments() {
        Permanent firstWing = harness.addToBattlefieldAndReturn(player1, new AzoriusFirstWing());
        Permanent enchantment = new Permanent(new Dovescape());
        Permanent creature = new Permanent(new AzoriusHerald());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, firstWing, enchantment)).isTrue();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, firstWing, creature)).isFalse();
    }
}
