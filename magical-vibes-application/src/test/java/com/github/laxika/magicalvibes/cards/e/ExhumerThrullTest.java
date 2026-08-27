package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExhumerThrull.class, GiantGrowth.class, GrizzlyBears.class, LightningBolt.class})
class ExhumerThrullTest extends BaseCardTest {

    @Test
    void enteringReturnsTargetCreatureCardFromGraveyardToHand() {
        Card creature = new GrizzlyBears();
        Card noncreature = new GiantGrowth();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setHand(player1, List.of(new ExhumerThrull()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Giant Growth");
    }

    @Test
    void hauntedCreatureDeathReturnsTargetCreatureCardFromGraveyardToHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID hauntedCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ExhumerThrull()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        UUID exhumerId = harness.getPermanentId(player1, "Exhumer Thrull");
        destroyWithLightningBolt(exhumerId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hauntedCreatureId);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Exhumer Thrull");

        destroyWithLightningBolt(hauntedCreatureId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void destroyWithLightningBolt(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
    }
}
