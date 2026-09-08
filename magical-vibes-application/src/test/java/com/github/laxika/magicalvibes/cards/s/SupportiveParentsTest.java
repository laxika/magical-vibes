package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SupportiveParents.class, GrizzlyBears.class})
class SupportiveParentsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two untapped creatures adds one mana of the chosen color")
    void tapsTwoCreaturesForAnyColorMana() {
        Permanent source = addCreatureReady(player1, new SupportiveParents());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without two untapped creatures you control")
    void cannotActivateWithoutTwoCreatures() {
        Permanent source = addCreatureReady(player1, new SupportiveParents());
        addCreatureReady(player2, new GrizzlyBears());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }
}
