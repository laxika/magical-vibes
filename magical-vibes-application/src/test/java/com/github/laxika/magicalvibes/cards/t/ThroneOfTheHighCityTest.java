package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ThroneOfTheHighCity.class)
class ThroneOfTheHighCityTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfTheHighCity());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(throne.isTapped()).isTrue();
    }

    @Test
    void sacrificesItselfAndMakesItsControllerTheMonarch() {
        harness.addToBattlefield(player1, new ThroneOfTheHighCity());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Throne of the High City");
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void cannotActivateTheMonarchAbilityWithoutFourMana() {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfTheHighCity());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(throne.isTapped()).isFalse();
        assertThat(gd.monarchPlayerId).isNull();
    }
}
