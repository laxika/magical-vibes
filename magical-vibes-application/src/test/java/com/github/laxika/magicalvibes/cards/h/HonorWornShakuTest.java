package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HonorWornShakuTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Honor-Worn Shaku adds one colorless mana")
    void tapAddsColorlessMana() {
        harness.addToBattlefield(player1, new HonorWornShaku());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanent(player1, "Honor-Worn Shaku").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping a legendary permanent untaps Honor-Worn Shaku, letting it make mana again")
    void tapLegendaryUntapsShaku() {
        harness.addToBattlefield(player1, new HonorWornShaku());
        harness.addToBattlefield(player1, new ArvadTheCursed());

        harness.tapPermanent(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Honor-Worn Shaku").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Arvad the Cursed").isTapped()).isTrue();

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot untap Honor-Worn Shaku with only a nonlegendary permanent")
    void cannotUntapWithNonlegendaryPermanent() {
        harness.addToBattlefield(player1, new HonorWornShaku());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot untap Honor-Worn Shaku when the only legendary permanent is already tapped")
    void cannotUntapWithTappedLegendary() {
        harness.addToBattlefield(player1, new HonorWornShaku());
        harness.addToBattlefield(player1, new ArvadTheCursed());
        findPermanent(player1, "Arvad the Cursed").tap();

        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
