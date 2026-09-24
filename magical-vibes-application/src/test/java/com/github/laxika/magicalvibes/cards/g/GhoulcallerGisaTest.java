package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhoulcallerGisa.class, GrizzlyBears.class})
class GhoulcallerGisaTest extends BaseCardTest {

    @Test
    @DisplayName("Creates Zombies equal to the sacrificed creature's power")
    void createsTokensEqualToSacrificedPower() {
        Permanent gisa = addReadyGisa(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
        assertThat(findPermanents(player1, "Zombie"))
                .allSatisfy(zombie -> {
                    assertThat(zombie.getCard().getPower()).isEqualTo(2);
                    assertThat(zombie.getCard().getToughness()).isEqualTo(2);
                    assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
                    assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
                });
        assertThat(gisa.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Uses the sacrificed creature's effective power")
    void usesEffectivePower() {
        addReadyGisa(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot sacrifice Ghoulcaller Gisa itself")
    void cannotSacrificeSelf() {
        addReadyGisa(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyGisa(Player player) {
        return addCreatureReady(player, new GhoulcallerGisa());
    }
}
