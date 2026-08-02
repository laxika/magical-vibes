package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommanderGrevenIlVecTest extends BaseCardTest {

    // "When Commander Greven il-Vec enters, sacrifice a creature."

    @Test
    @DisplayName("Alone on the battlefield, Greven sacrifices itself")
    void aloneSacrificesItself() {
        harness.setHand(player1, List.of(new CommanderGrevenIlVec()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Commander Greven il-Vec");
        harness.assertInGraveyard(player1, "Commander Greven il-Vec");
    }

    @Test
    @DisplayName("With another creature, controller is prompted and may spare Greven")
    void controllerChoosesWhichCreatureToSacrifice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());

        harness.setHand(player1, List.of(new CommanderGrevenIlVec()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Commander Greven il-Vec");
        harness.assertOnBattlefield(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Controller may sacrifice Greven itself even with other creatures available")
    void mayChooseGrevenItself() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new CommanderGrevenIlVec()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Commander Greven il-Vec"));

        harness.assertNotOnBattlefield(player1, "Commander Greven il-Vec");
        harness.assertInGraveyard(player1, "Commander Greven il-Vec");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent's creatures are never sacrificed")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());

        harness.setHand(player1, List.of(new CommanderGrevenIlVec()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Giant Spider"));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertOnBattlefield(player1, "Commander Greven il-Vec");
    }
}
