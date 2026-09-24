package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommandersSphere.class, GrizzlyBears.class, EdgarMarkov.class, Plains.class})
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
    @DisplayName("Sacrificing the sphere draws a card without requiring it to be untapped")
    void sacrificingDrawsACardWithoutTapCost() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        harness.addToBattlefield(player1, new CommandersSphere());
        harness.setLibrary(player1, List.of(new Plains()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInGraveyard(player1, "Commander's Sphere");
    }

    @Test
    @DisplayName("Sacrificing it draws a card")
    void sacrificingItDrawsACard() {
        harness.addToBattlefield(player1, new CommandersSphere());
        harness.setLibrary(player1, List.of(new Plains()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Commander's Sphere");
        harness.assertInGraveyard(player1, "Commander's Sphere");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }
}
