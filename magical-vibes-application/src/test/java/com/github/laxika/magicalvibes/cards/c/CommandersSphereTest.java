package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommandersSphere.class, GrizzlyBears.class})
class CommandersSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Commander's Sphere adds one mana of the chosen color")
    void tappingAddsChosenColorMana() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new CommandersSphere());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(sphere.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing Commander's Sphere draws a card")
    void sacrificingDrawsCard() {
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new CommandersSphere());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(handSizeBefore + 1)
                .contains(drawn);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sphere);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sphere.getCard());
    }
}
