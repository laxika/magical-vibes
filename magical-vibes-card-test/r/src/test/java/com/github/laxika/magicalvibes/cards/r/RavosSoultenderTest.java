package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavosSoultender.class, GrizzlyBears.class, Forest.class})
class RavosSoultenderTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get +1/+1, but Ravos and opponents' creatures do not")
    void boostsOtherControlledCreatures() {
        Permanent ravos = addCreatureReady(player1, new RavosSoultender());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ravos)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ravos)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("At upkeep, Ravos may return a creature card from its controller's graveyard to hand")
    void returnsTargetCreatureCardToHand() {
        Card creature = new GrizzlyBears();
        Card nonCreature = new Forest();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        addCreatureReady(player1, new RavosSoultender());

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(creature.getId());
    }

    @Test
    @DisplayName("The upkeep return is optional")
    void mayDeclineToReturnCreatureCard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addCreatureReady(player1, new RavosSoultender());

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId());
        harness.assertNotInHand(player1, "Grizzly Bears");
    }
}
