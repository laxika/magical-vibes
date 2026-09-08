package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NethroiApexOfDeath.class, GrizzlyBears.class, HillGiant.class})
class NethroiApexOfDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating returns any number of target creature cards with total power 10 or less")
    void mutatingReturnsCreatureCardsWithinTotalPowerLimit() {
        Permanent nethroi = addCreatureReady(player1, new NethroiApexOfDeath());
        Card bear = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        Card secondBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear, hillGiant, secondBear));

        triggerMutation(nethroi);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bear.getId(), hillGiant.getId(), secondBear.getId());

        harness.handleMultipleCardsChosen(player1,
                List.of(bear.getId(), hillGiant.getId(), secondBear.getId()));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(countPermanents(player1, "Hill Giant")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mutating rejects a target selection whose total power exceeds 10")
    void mutatingRejectsSelectionAboveTotalPowerLimit() {
        Permanent nethroi = addCreatureReady(player1, new NethroiApexOfDeath());
        List<Card> hillGiants = List.of(new HillGiant(), new HillGiant(), new HillGiant(), new HillGiant());
        harness.setGraveyard(player1, hillGiants);

        triggerMutation(nethroi);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, hillGiants.stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 10");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();

        harness.handleMultipleCardsChosen(player1,
                hillGiants.subList(0, 3).stream().map(Card::getId).toList());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Hill Giant")).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(hillGiants.get(3));
    }

    @Test
    @DisplayName("Mutating with no creature cards still creates an optional trigger")
    void mutatingWithNoCreatureCardsCreatesNoTargetsTrigger() {
        Permanent nethroi = addCreatureReady(player1, new NethroiApexOfDeath());

        triggerMutation(nethroi);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
    }

    private void triggerMutation(Permanent nethroi) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, nethroi, List.of(nethroi.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSelfTriggeredAbilityTarget(gd));
    }
}
