package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CircleOfElders.class, AvatarOfMight.class})
class CircleOfEldersTest extends BaseCardTest {

    @Test
    @DisplayName("Adds three colorless mana when your creatures have total power 8 or greater")
    void addsThreeColorlessManaWhenFormidable() {
        Permanent circle = addCreatureReady(player1, new CircleOfElders());
        addCreatureReady(player1, new AvatarOfMight());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(circle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate at exactly eight total power")
    void canActivateAtExactThreshold() {
        Permanent circle = addCreatureReady(player1, new CircleOfElders());
        addCreatureReady(player1, new AvatarOfMight());
        circle.setPowerModifier(-2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate below eight total power")
    void cannotActivateBelowThreshold() {
        Permanent circle = addCreatureReady(player1, new CircleOfElders());
        addCreatureReady(player1, new AvatarOfMight());
        circle.setPowerModifier(-3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 8 or greater");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(circle.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Counts only creatures you control")
    void countsOnlyControlledCreatures() {
        Permanent circle = addCreatureReady(player1, new CircleOfElders());
        addCreatureReady(player2, new AvatarOfMight());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 8 or greater");
        assertThat(circle.isTapped()).isFalse();
    }
}
