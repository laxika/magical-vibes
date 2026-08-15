package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VexingScuttlerTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, returns a targeted instant from the graveyard to hand")
    void castTriggerReturnsInstant() {
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt));
        harness.setHand(player1, List.of(new VexingScuttler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bolt.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Lightning Bolt"));
        harness.assertOnBattlefield(player1, "Vexing Scuttler");
    }

    @Test
    @DisplayName("Emerge cast trigger returns a targeted sorcery from the graveyard")
    void emergeCastTriggerReturnsSorcery() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        harness.setHand(player1, List.of(new VexingScuttler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Divination");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Divination"));
        harness.assertOnBattlefield(player1, "Vexing Scuttler");
    }

    @Test
    @DisplayName("Does not trigger when the graveyard has no instant or sorcery card")
    void noTriggerForNonMatchingGraveyardCard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new VexingScuttler()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vexing Scuttler");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
