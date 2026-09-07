package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SisaysRing.class})
class SisaysRingTest extends BaseCardTest {

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for mana adds two colorless mana")
    void tapForTwoColorlessMana() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SisaysRing());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(ring.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability resolves without using the stack")
    void manaAbilityDoesNotUseStack() {
        harness.addToBattlefield(player1, new SisaysRing());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
    }
}
