package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CeruleanWisps;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HallOfOraclesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana without using the stack")
    void tapForColorless() {
        harness.addToBattlefield(player1, new HallOfOracles());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The filter ability spends {1} and adds one mana of the chosen color")
    void filterAddsChosenColor() {
        harness.addToBattlefield(player1, new HallOfOracles());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void putsCounterAfterCastingInstantOrSorcery() {
        harness.addToBattlefield(player1, new HallOfOracles());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CeruleanWisps()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.activateAbility(player1, 0, 2, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterAbilityRequiresInstantOrSorceryThisTurn() {
        harness.addToBattlefield(player1, new HallOfOracles());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery");
    }

    @Test
    void counterAbilityCannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new HallOfOracles());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CeruleanWisps()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
