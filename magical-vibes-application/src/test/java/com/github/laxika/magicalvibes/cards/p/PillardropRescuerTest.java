package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PillardropRescuerTest extends BaseCardTest {

    private void castPillardropRescuer() {
        harness.setHand(player1, List.of(new PillardropRescuer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted creature card with mana value 3 or less to hand")
    void returnsEligibleCreatureToHand() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        castPillardropRescuer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB only targets own creature cards with mana value 3 or less")
    void filtersGraveyardTargets() {
        Card eligible = new GrizzlyBears();
        Card nonCreature = new LightningBolt();
        Card tooExpensive = new HillGiant();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible, nonCreature, tooExpensive));
        harness.setGraveyard(player2, List.of(opponentCreature));

        castPillardropRescuer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
    }

    @Test
    @DisplayName("ETB does nothing when no eligible creature card is in the graveyard")
    void noEligibleCreatureProducesNoChoice() {
        harness.setGraveyard(player1, List.of(new LightningBolt(), new HillGiant()));

        castPillardropRescuer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
