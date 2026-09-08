package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EndriderCatalyzerTest extends BaseCardTest {

    @Test
    void startsEnginesWhenItEntersTheBattlefield() {
        harness.setHand(player1, List.of(new EndriderCatalyzer()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void addsTwoRedManaAtMaxSpeed() {
        Permanent catalyzer = addCreatureReady(player1, new EndriderCatalyzer());
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(catalyzer.isTapped()).isTrue();
    }

    @Test
    void cannotActivateBeforeMaxSpeed() {
        Permanent catalyzer = addCreatureReady(player1, new EndriderCatalyzer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(catalyzer.isTapped()).isFalse();
    }
}
