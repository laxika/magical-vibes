package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommandersSphere.class, EdgarMarkov.class, GrizzlyBears.class})
class CommandersSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Produces mana only from the commander's color identity")
    void producesManaInCommandersColorIdentity() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        harness.addToBattlefield(player1, new CommandersSphere());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("WHITE", "BLACK", "RED");

        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rejects a color outside the commander's color identity")
    void rejectsColorOutsideCommandersIdentity() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        harness.addToBattlefield(player1, new CommandersSphere());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.GREEN.name()))
                .isInstanceOf(IllegalArgumentException.class);

        harness.handleListChoice(player1, ManaColor.WHITE.name());
    }

    @Test
    @DisplayName("Sacrificing Commander's Sphere draws a card")
    void sacrificesAndDraws() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new CommandersSphere());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sphere);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sphere.getCard());
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
    }
}
