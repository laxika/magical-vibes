package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CircleOfDreamsDruid.class, Forest.class})
class CircleOfDreamsDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Circle of Dreams Druid adds one green mana for each creature you control")
    void addsGreenManaForEachControlledCreature() {
        addCreatureReady(player1, new CircleOfDreamsDruid());
        harness.addToBattlefield(player1, new CircleOfDreamsDruid());
        harness.addToBattlefield(player1, new CircleOfDreamsDruid());
        harness.addToBattlefield(player2, new CircleOfDreamsDruid());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Druid counts itself but not lands, and its mana ability resolves immediately")
    void countsItselfAndResolvesWithoutUsingStack() {
        var druid = addCreatureReady(player1, new CircleOfDreamsDruid());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Druid cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        var druid = harness.addToBattlefieldAndReturn(player1, new CircleOfDreamsDruid());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(druid.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A tapped Druid cannot produce mana a second time")
    void cannotActivateAgainWithoutUntapping() {
        addCreatureReady(player1, new CircleOfDreamsDruid());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
