package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhispererOfTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one green mana")
    void firstAbilityAddsOneGreenMana() {
        addReadyWhisperer();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ferocious ability adds two green mana when controlling a creature with power 4 or greater")
    void ferociousAbilityAddsTwoGreenMana() {
        addReadyWhisperer();
        harness.addToBattlefield(player1, new AvatarOfMight());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ferocious ability cannot activate without a creature with power 4 or greater")
    void ferociousAbilityRequiresBigCreatureYouControl() {
        addReadyWhisperer();
        harness.addToBattlefield(player2, new AvatarOfMight());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private Permanent addReadyWhisperer() {
        Permanent whisperer = harness.addToBattlefieldAndReturn(player1, new WhispererOfTheWilds());
        whisperer.setSummoningSick(false);
        return whisperer;
    }
}
