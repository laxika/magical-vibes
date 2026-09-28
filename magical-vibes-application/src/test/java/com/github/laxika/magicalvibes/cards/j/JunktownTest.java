package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Junktown.class)
class JunktownTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for colorless mana")
    void tapsForColorlessMana() {
        Permanent junktown = harness.addToBattlefieldAndReturn(player1, new Junktown());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(junktown.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Junktown creates three Junk tokens")
    void sacrificesToCreateJunkTokens() {
        harness.addToBattlefield(player1, new Junktown());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Junk"))
                .hasSize(3);
        harness.assertNotOnBattlefield(player1, "Junktown");
        harness.assertInGraveyard(player1, "Junktown");
    }
}
